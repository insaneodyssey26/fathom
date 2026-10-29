const MAX_DEVICES_PER_KEY = 2;
const EXPIRY_GRACE_MS = 3 * 24 * 60 * 60 * 1000;
const MISSING_KEY_TTL_SECONDS = 30 * 24 * 60 * 60;
const PENDING_SUBSCRIPTION_TTL_SECONDS = 7 * 24 * 60 * 60;
const BLOCKED_STATUSES = ["REVOKED", "REFUNDED", "EXPIRED"];
const SUBSCRIPTION_RENEWAL_EVENTS = new Set([
  "subscription.active",
  "subscription.renewed",
  "subscription.updated",
  "subscription.plan_changed"
]);
const KEY_BEARING_EVENTS = new Set(["payment.succeeded", "license_key.created"]);

function parseTimestampMs(value) {
  if (typeof value === "number") {
    return Number.isFinite(value) && value > 0 ? value : 0;
  }
  const parsed = Date.parse(value || "");
  return Number.isFinite(parsed) ? parsed : 0;
}

function blockedStatus(record, now = Date.now()) {
  if (BLOCKED_STATUSES.includes(record.status)) {
    return record.status;
  }
  if (record.expiresAt && now > record.expiresAt + EXPIRY_GRACE_MS) {
    return "EXPIRED";
  }
  return null;
}

function deriveSubscriptionUpdate(eventType, subscription, now = Date.now()) {
  const nextBilling = parseTimestampMs(subscription.next_billing_date);
  const cancelAtPeriodEnd = subscription.cancel_at_next_billing_date === true;

  if (eventType === "subscription.expired") {
    return { expire: true };
  }
  if (eventType === "subscription.cancelled") {
    if (nextBilling > now) {
      return { expire: false, expiresAt: nextBilling, cancelAtPeriodEnd: true };
    }
    return { expire: true };
  }
  if (SUBSCRIPTION_RENEWAL_EVENTS.has(eventType) && subscription.status === "active" && nextBilling > now) {
    return { expire: false, expiresAt: nextBilling, cancelAtPeriodEnd };
  }
  return null;
}

function applySubscriptionUpdate(record, update, eventType, now = Date.now()) {
  if (record.status !== "ACTIVE") {
    return false;
  }
  if (update.expire) {
    record.status = "EXPIRED";
    record.expiredReason = eventType;
    record.expiredAt = now;
    return true;
  }
  record.expiresAt = update.expiresAt;
  record.cancelAtPeriodEnd = update.cancelAtPeriodEnd;
  return true;
}

async function resolveLicenseKey(env, payload, directKey) {
  if (directKey) {
    return directKey;
  }
  const lookups = [];
  if (typeof payload.payment_id === "string" && payload.payment_id) {
    lookups.push(`ORDER:${payload.payment_id}`);
  }
  if (typeof payload.subscription_id === "string" && payload.subscription_id) {
    lookups.push(`SUB:${payload.subscription_id}`);
  }
  for (const lookup of lookups) {
    const indexed = await env.FATHOM_LICENSES.get(lookup);
    if (indexed) {
      return indexed;
    }
  }
  return null;
}

function base64ToUint8Array(base64) {
  const binaryString = atob(base64);
  const bytes = new Uint8Array(binaryString.length);
  for (let i = 0; i < binaryString.length; i++) {
    bytes[i] = binaryString.charCodeAt(i);
  }
  return bytes;
}

function uint8ArrayToBase64(bytes) {
  let binary = "";
  for (let i = 0; i < bytes.byteLength; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return btoa(binary);
}

async function signLicensePayload(payloadJson, env) {
  const encoder = new TextEncoder();
  const dataBytes = encoder.encode(payloadJson);

  const privateKeyB64 = (env && env.LICENSE_SIGNING_KEY) || "";
  if (privateKeyB64) {
    try {
      const rawBinary = Uint8Array.from(atob(privateKeyB64), c => c.charCodeAt(0));
      const privKey = await crypto.subtle.importKey(
        "pkcs8",
        rawBinary,
        { name: "Ed25519" },
        false,
        ["sign"]
      );
      const signature = await crypto.subtle.sign({ name: "Ed25519" }, privKey, dataBytes);
      return uint8ArrayToBase64(new Uint8Array(signature));
    } catch (_) {}
  }

  const secretKey = (env && env.LICENSE_SIGNING_SECRET) || "fathom_sanctuary_secret_key";
  const keyBytes = encoder.encode(secretKey);

  const cryptoKey = await crypto.subtle.importKey(
    "raw",
    keyBytes,
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"]
  );

  const signature = await crypto.subtle.sign("HMAC", cryptoKey, dataBytes);
  return uint8ArrayToBase64(new Uint8Array(signature));
}

async function verifyStandardWebhookSignature(rawBody, headers, secret) {
  if (!secret) return false;

  const webhookId = headers.get("webhook-id");
  const webhookTimestamp = headers.get("webhook-timestamp");
  const webhookSignature = headers.get("webhook-signature") || headers.get("x-dodo-signature");

  if (!webhookId || !webhookTimestamp || !webhookSignature) {
    return false;
  }

  const timestampNum = parseInt(webhookTimestamp, 10);
  const now = Math.floor(Date.now() / 1000);
  if (isNaN(timestampNum) || Math.abs(now - timestampNum) > 300) {
    return false;
  }

  const signedContent = `${webhookId}.${webhookTimestamp}.${rawBody}`;
  const encoder = new TextEncoder();
  const signedContentBytes = encoder.encode(signedContent);

  let secretKeyBytes;
  if (secret.startsWith("whsec_")) {
    secretKeyBytes = base64ToUint8Array(secret.slice(6));
  } else {
    try {
      secretKeyBytes = base64ToUint8Array(secret);
    } catch {
      secretKeyBytes = encoder.encode(secret);
    }
  }

  const cryptoKey = await crypto.subtle.importKey(
    "raw",
    secretKeyBytes,
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"]
  );

  const signatureBuffer = await crypto.subtle.sign("HMAC", cryptoKey, signedContentBytes);
  const expectedSignatureBase64 = uint8ArrayToBase64(new Uint8Array(signatureBuffer));

  const signatures = webhookSignature.split(" ");
  for (const sigItem of signatures) {
    const parts = sigItem.split(",");
    const sig = parts.length === 2 ? parts[1] : parts[0];
    if (sig === expectedSignatureBase64) {
      return true;
    }
  }

  return false;
}

const PRODUCT_TIER_MAP = {
  "pdt_0npsdymrmyfzfotei1iid": { tier: "MONTHLY", prefix: "FATHOM-MONTHLY" },
  "pdt_0npsfciocfksq1u8dv2nn": { tier: "ANNUAL", prefix: "FATHOM-ANNUAL" },
  "pdt_0npsfevfdb1dxri9yhtue": { tier: "LIFETIME", prefix: "FATHOM-LIFETIME" },
  "pdt_fathom_monthly": { tier: "MONTHLY", prefix: "FATHOM-MONTHLY" },
  "pdt_fathom_annual": { tier: "ANNUAL", prefix: "FATHOM-ANNUAL" },
  "pdt_fathom_lifetime": { tier: "LIFETIME", prefix: "FATHOM-LIFETIME" }
};

function toLicenseKeyString(candidate) {
  if (typeof candidate === "string") {
    return candidate.trim();
  }
  if (candidate && typeof candidate === "object") {
    return toLicenseKeyString(candidate.key || candidate.license_key || candidate.value);
  }
  return "";
}

function extractLicenseKey(candidates) {
  for (const candidate of candidates) {
    const key = toLicenseKeyString(candidate);
    if (key) {
      return key.toUpperCase();
    }
  }
  return null;
}

function resolveProductTier(productId, productName) {
  const cleanId = (productId || "").trim().toLowerCase();
  if (PRODUCT_TIER_MAP[cleanId]) {
    return PRODUCT_TIER_MAP[cleanId];
  }

  const cleanName = (productName || "").trim().toUpperCase();
  if (cleanName.includes("MONTHLY") || cleanId.includes("monthly")) {
    return { tier: "MONTHLY", prefix: "FATHOM-MONTHLY" };
  }
  if (cleanName.includes("ANNUAL") || cleanName.includes("YEAR") || cleanId.includes("annual") || cleanId.includes("year")) {
    return { tier: "ANNUAL", prefix: "FATHOM-ANNUAL" };
  }
  return { tier: "LIFETIME", prefix: "FATHOM-LIFETIME" };
}

export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);
    const clientIp = request.headers.get("cf-connecting-ip") || "unknown";

    if (request.method === "OPTIONS") {
      return new Response(null, {
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
          "Access-Control-Allow-Headers": "Content-Type, Authorization, webhook-id, webhook-timestamp, webhook-signature, x-dodo-signature"
        }
      });
    }

    if (url.pathname === "/health") {
      return new Response(JSON.stringify({ status: "ok", service: "Fathom Sacred License Engine", maxDevices: MAX_DEVICES_PER_KEY }), {
        headers: { "Content-Type": "application/json" }
      });
    }

    if (url.pathname === "/api/devices" && request.method === "GET") {
      try {
        const licenseKey = (url.searchParams.get("licenseKey") || "").trim().toUpperCase();
        const currentDeviceId = (url.searchParams.get("deviceId") || "").trim();

        if (!licenseKey) {
          return new Response(JSON.stringify({ success: false, error: "License key is required" }), {
            status: 400,
            headers: { "Content-Type": "application/json" }
          });
        }

        const kvData = await env.FATHOM_LICENSES.get(licenseKey, { type: "json" });
        if (!kvData) {
          return new Response(JSON.stringify({ success: false, error: "License not found" }), {
            status: 404,
            headers: { "Content-Type": "application/json" }
          });
        }

        const callerDeviceId = (currentDeviceId || "").trim();
        const activeList = Array.isArray(kvData.activatedDevices) ? kvData.activatedDevices : [];
        if (callerDeviceId && activeList.length > 0) {
          const isCallerAuthorized = activeList.some(d => d.deviceId === callerDeviceId);
          if (!isCallerAuthorized) {
            return new Response(JSON.stringify({ success: false, error: "Unauthorized: caller device is not recognized on this license" }), {
              status: 403,
              headers: { "Content-Type": "application/json" }
            });
          }
        }

        const blocked = blockedStatus(kvData);
        if (blocked) {
          return new Response(JSON.stringify({
            success: false,
            error: `License is ${blocked.toLowerCase()}`,
            status: blocked
          }), {
            status: 403,
            headers: { "Content-Type": "application/json" }
          });
        }

        const devices = (kvData.activatedDevices || []).map(d => ({
          deviceId: d.deviceId,
          deviceName: d.deviceName || "Device",
          activatedAt: d.activatedAt || 0,
          isCurrent: currentDeviceId ? d.deviceId === currentDeviceId : false
        }));

        return new Response(JSON.stringify({
          success: true,
          tier: kvData.tier || "LIFETIME",
          expiresAt: kvData.expiresAt || 0,
          deviceCount: devices.length,
          maxDevices: kvData.maxDevices || MAX_DEVICES_PER_KEY,
          devices: devices
        }), {
          headers: { "Content-Type": "application/json" }
        });
      } catch (err) {
        return new Response(JSON.stringify({ success: false, error: err.message }), {
          status: 500,
          headers: { "Content-Type": "application/json" }
        });
      }
    }

    if (url.pathname === "/api/activate" && request.method === "POST") {
      try {
        const body = await request.json();
        const licenseKey = (body.licenseKey || "").trim().toUpperCase();
        const email = (body.email || "").trim().toLowerCase();
        const deviceId = (body.deviceId || "").trim();
        const deviceName = (body.deviceName || "Fathom Reader").trim();

        if (!licenseKey || !deviceId) {
          return new Response(JSON.stringify({ success: false, error: "License key and device ID are required" }), {
            status: 400,
            headers: { "Content-Type": "application/json" }
          });
        }

        if (licenseKey.includes("@")) {
          return new Response(JSON.stringify({
            success: false,
            error: "Please enter your License Key, not your email address. You can find it in your purchase receipt."
          }), {
            status: 400,
            headers: { "Content-Type": "application/json" }
          });
        }

        const kvData = await env.FATHOM_LICENSES.get(licenseKey, { type: "json" });
        if (!kvData) {
          return new Response(JSON.stringify({ success: false, error: "License key not found. Please verify your purchase." }), {
            status: 404,
            headers: { "Content-Type": "application/json" }
          });
        }

        const blocked = blockedStatus(kvData);
        if (blocked) {
          return new Response(JSON.stringify({
            success: false,
            error: `License is ${blocked.toLowerCase()}. Activation denied.`,
            status: blocked
          }), {
            status: 403,
            headers: { "Content-Type": "application/json" }
          });
        }

        if (!Array.isArray(kvData.activatedDevices)) {
          kvData.activatedDevices = [];
        }

        const existingDeviceIndex = kvData.activatedDevices.findIndex(d => d.deviceId === deviceId);

        if (existingDeviceIndex >= 0) {
          kvData.activatedDevices[existingDeviceIndex].lastSeenAt = Date.now();
          if (deviceName && deviceName !== "Fathom Reader") {
            kvData.activatedDevices[existingDeviceIndex].deviceName = deviceName;
          }
          await env.FATHOM_LICENSES.put(licenseKey, JSON.stringify(kvData));

          const devices = kvData.activatedDevices.map(d => ({
            deviceId: d.deviceId,
            deviceName: d.deviceName || "Device",
            activatedAt: d.activatedAt || 0,
            isCurrent: d.deviceId === deviceId
          }));

          const tokenData = JSON.stringify({
            licenseKey,
            tier: kvData.tier,
            deviceId,
            expiresAt: kvData.expiresAt || 0,
            issuedAt: Date.now()
          });
          const signature = await signLicensePayload(tokenData, env);

          return new Response(JSON.stringify({
            success: true,
            tier: kvData.tier,
            expiresAt: kvData.expiresAt || 0,
            deviceCount: kvData.activatedDevices.length,
            maxDevices: kvData.maxDevices || MAX_DEVICES_PER_KEY,
            devices: devices,
            signedToken: tokenData,
            tokenSignature: signature,
            message: "Sanctuary+ Patron restored on this device."
          }), {
            headers: { "Content-Type": "application/json" }
          });
        }

        const maxDevices = kvData.maxDevices || MAX_DEVICES_PER_KEY;
        if (kvData.activatedDevices.length >= maxDevices) {
          return new Response(JSON.stringify({
            success: false,
            error: `Device limit reached (${kvData.activatedDevices.length}/${maxDevices} active devices). Please revoke an old device in Settings.`
          }), {
            status: 403,
            headers: { "Content-Type": "application/json" }
          });
        }

        kvData.activatedDevices.push({
          deviceId: deviceId,
          deviceName: deviceName,
          activatedAt: Date.now(),
          lastSeenAt: Date.now()
        });

        if (email && !kvData.email) {
          kvData.email = email;
        }

        await env.FATHOM_LICENSES.put(licenseKey, JSON.stringify(kvData));

        const devices = kvData.activatedDevices.map(d => ({
          deviceId: d.deviceId,
          deviceName: d.deviceName || "Device",
          activatedAt: d.activatedAt || 0,
          isCurrent: d.deviceId === deviceId
        }));

        const tokenData = JSON.stringify({
          licenseKey,
          tier: kvData.tier,
          deviceId,
          expiresAt: kvData.expiresAt || 0,
          issuedAt: Date.now()
        });
        const signature = await signLicensePayload(tokenData, env);

        return new Response(JSON.stringify({
          success: true,
          tier: kvData.tier,
          expiresAt: kvData.expiresAt || 0,
          deviceCount: kvData.activatedDevices.length,
          maxDevices: maxDevices,
          devices: devices,
          signedToken: tokenData,
          tokenSignature: signature,
          message: "Welcome, Sanctuary+ Patron. Sanctuary unlocked."
        }), {
          headers: { "Content-Type": "application/json" }
        });

      } catch (err) {
        return new Response(JSON.stringify({ success: false, error: err.message }), {
          status: 500,
          headers: { "Content-Type": "application/json" }
        });
      }
    }

    if ((url.pathname === "/api/revoke" || url.pathname === "/api/deactivate") && request.method === "POST") {
      try {
        const body = await request.json();
        const licenseKey = (body.licenseKey || "").trim().toUpperCase();
        const deviceId = (body.deviceId || "").trim();

        if (!licenseKey || !deviceId) {
          return new Response(JSON.stringify({ success: false, error: "License key and device ID are required" }), {
            status: 400,
            headers: { "Content-Type": "application/json" }
          });
        }

        const kvData = await env.FATHOM_LICENSES.get(licenseKey, { type: "json" });
        if (!kvData) {
          return new Response(JSON.stringify({ success: false, error: "License not found" }), {
            status: 404,
            headers: { "Content-Type": "application/json" }
          });
        }

        const callerDeviceId = (body.callerDeviceId || "").trim();
        const activeList = Array.isArray(kvData.activatedDevices) ? kvData.activatedDevices : [];
        if (callerDeviceId && activeList.length > 0) {
          const isCallerAuthorized = activeList.some(d => d.deviceId === callerDeviceId);
          if (!isCallerAuthorized) {
            return new Response(JSON.stringify({ success: false, error: "Unauthorized: caller device is not recognized on this license" }), {
              status: 403,
              headers: { "Content-Type": "application/json" }
            });
          }
        }

        if (Array.isArray(kvData.activatedDevices)) {
          kvData.activatedDevices = kvData.activatedDevices.filter(d => d.deviceId !== deviceId);
          await env.FATHOM_LICENSES.put(licenseKey, JSON.stringify(kvData));
        }

        const devices = (kvData.activatedDevices || []).map(d => ({
          deviceId: d.deviceId,
          deviceName: d.deviceName || "Device",
          activatedAt: d.activatedAt || 0,
          isCurrent: false
        }));

        return new Response(JSON.stringify({
          success: true,
          deviceCount: devices.length,
          maxDevices: kvData.maxDevices || MAX_DEVICES_PER_KEY,
          devices: devices,
          message: "Device slot released successfully."
        }), {
          headers: { "Content-Type": "application/json" }
        });
      } catch (err) {
        return new Response(JSON.stringify({ success: false, error: err.message }), {
          status: 500,
          headers: { "Content-Type": "application/json" }
        });
      }
    }

    const isWebhookPath = (
      url.pathname === "/api/webhook/dodo" ||
      url.pathname === "/api/webhook" ||
      url.pathname === "/webhook" ||
      url.pathname === "/dodo" ||
      url.pathname === "/api/dodo" ||
      url.pathname === "/webhook/dodo"
    );

    if (isWebhookPath && request.method === "GET") {
      return new Response(JSON.stringify({ status: "ok", service: "Fathom Dodo Webhook Receiver" }), {
        headers: { "Content-Type": "application/json" }
      });
    }

    if (isWebhookPath && request.method === "POST") {
      try {
        const rawBody = await request.text();

        if (!env.DODO_WEBHOOK_SECRET) {
          console.error("[DODO WEBHOOK] DODO_WEBHOOK_SECRET is not configured; refusing to process webhooks");
          return new Response(JSON.stringify({ success: false, error: "Webhook receiver is not configured" }), {
            status: 503,
            headers: { "Content-Type": "application/json" }
          });
        }

        const isValid = await verifyStandardWebhookSignature(rawBody, request.headers, env.DODO_WEBHOOK_SECRET);
        if (!isValid) {
          return new Response(JSON.stringify({ success: false, error: "Invalid webhook signature" }), {
            status: 401,
            headers: { "Content-Type": "application/json" }
          });
        }

        const eventData = JSON.parse(rawBody);
        const eventType = (eventData.type || eventData.event || "").toLowerCase();
        const payload = eventData.data || eventData.payload || eventData;

        const dodoKey = extractLicenseKey([
          payload.license_key,
          Array.isArray(payload.license_keys) ? payload.license_keys[0] : null,
          payload.license_key_instance?.key,
          payload.licenses?.[0]?.key,
          payload.licenseKey,
          payload.key,
          eventData.data?.license_key,
          Array.isArray(eventData.data?.license_keys) ? eventData.data.license_keys[0] : null
        ]);

        const paymentId = payload.payment_id || payload.id || `TXN_${Date.now()}`;
        const rawCustomerEmail = (
          payload.customer?.email ||
          payload.billing?.email ||
          payload.email ||
          payload.customer_email ||
          eventData.customer?.email ||
          ""
        ).trim().toLowerCase();

        const customerId = payload.customer_id || payload.customer?.id || payload.customer?.customer_id || "";

        if (rawCustomerEmail) {
          if (paymentId) {
            await env.FATHOM_LICENSES.put(`PAYMENT_EMAIL:${paymentId}`, rawCustomerEmail, { expirationTtl: 86400 * 7 });
          }
          if (customerId) {
            await env.FATHOM_LICENSES.put(`CUST_EMAIL:${customerId}`, rawCustomerEmail, { expirationTtl: 86400 * 30 });
          }
        }

        let resolvedEmail = rawCustomerEmail;
        if (!resolvedEmail && paymentId) {
          resolvedEmail = (await env.FATHOM_LICENSES.get(`PAYMENT_EMAIL:${paymentId}`)) || "";
        }
        if (!resolvedEmail && customerId) {
          resolvedEmail = (await env.FATHOM_LICENSES.get(`CUST_EMAIL:${customerId}`)) || "";
        }

        const customerEmail = resolvedEmail;

        const productId = (payload.product_id || payload.items?.[0]?.product_id || "").toLowerCase();
        const productName = (payload.product_name || payload.items?.[0]?.product_name || "").toUpperCase();
        const mapped = resolveProductTier(productId, productName);

        const subscriptionId = typeof payload.subscription_id === "string" ? payload.subscription_id : "";

        if (eventType === "refund.succeeded" || eventType === "dispute.opened" || eventType === "dispute.lost") {
          const targetKey = await resolveLicenseKey(env, payload, dodoKey);
          if (targetKey) {
            const existing = await env.FATHOM_LICENSES.get(targetKey, { type: "json" });
            if (existing) {
              existing.status = "REVOKED";
              existing.revokedReason = eventType;
              existing.revokedAt = Date.now();
              await env.FATHOM_LICENSES.put(targetKey, JSON.stringify(existing));
            }
          }
          return new Response(JSON.stringify({ success: true, message: `License revoked due to ${eventType}` }), {
            headers: { "Content-Type": "application/json" }
          });
        }

        if (eventType.startsWith("subscription.")) {
          const update = deriveSubscriptionUpdate(eventType, payload);
          if (update) {
            const targetKey = await resolveLicenseKey(env, payload, dodoKey);
            const record = targetKey ? await env.FATHOM_LICENSES.get(targetKey, { type: "json" }) : null;
            if (record) {
              if (applySubscriptionUpdate(record, update, eventType)) {
                await env.FATHOM_LICENSES.put(targetKey, JSON.stringify(record));
              }
            } else if (!update.expire && subscriptionId) {
              await env.FATHOM_LICENSES.put(`SUBSCRIPTION_PENDING:${subscriptionId}`, JSON.stringify(update), { expirationTtl: PENDING_SUBSCRIPTION_TTL_SECONDS });
            }
          }
          return new Response(JSON.stringify({ success: true, message: `Subscription event ${eventType} processed` }), {
            headers: { "Content-Type": "application/json" }
          });
        }

        if (!dodoKey && !KEY_BEARING_EVENTS.has(eventType)) {
          return new Response(JSON.stringify({ success: true, ignored: true }), {
            headers: { "Content-Type": "application/json" }
          });
        }

        if (!dodoKey) {
          if (customerEmail && paymentId) {
            await env.FATHOM_LICENSES.put(`PENDING_KEY_ORDER:${paymentId}`, customerEmail, { expirationTtl: 86400 * 7 });
          }
          return new Response(JSON.stringify({
            success: true,
            message: "Payment captured. Waiting for license key generation event."
          }), {
            status: 200,
            headers: { "Content-Type": "application/json" }
          });
        }

        const licenseKey = dodoKey;

        await env.FATHOM_LICENSES.delete(`MISSING_KEY:${paymentId}`);

        const existingRecord = await env.FATHOM_LICENSES.get(licenseKey, { type: "json" });
        if (existingRecord) {
          if (!existingRecord.email && customerEmail) {
            existingRecord.email = customerEmail;
          }
          existingRecord.orderId = existingRecord.orderId || paymentId;
          existingRecord.tier = existingRecord.tier || mapped.tier;
          existingRecord.status = existingRecord.status || "ACTIVE";
          existingRecord.subscriptionId = existingRecord.subscriptionId || subscriptionId || null;
          await env.FATHOM_LICENSES.put(licenseKey, JSON.stringify(existingRecord));
          await env.FATHOM_LICENSES.put(`ORDER:${paymentId}`, licenseKey);
          if (existingRecord.email) {
            await env.FATHOM_LICENSES.put(`EMAIL:${existingRecord.email}`, licenseKey);
          }
          if (subscriptionId) {
            await env.FATHOM_LICENSES.put(`SUB:${subscriptionId}`, licenseKey);
          }

          return new Response(JSON.stringify({
            success: true,
            licenseKey: licenseKey,
            tier: existingRecord.tier,
            idempotent: true
          }), {
            headers: { "Content-Type": "application/json" }
          });
        }

        const licenseRecord = {
          key: licenseKey,
          tier: mapped.tier,
          email: customerEmail || null,
          orderId: paymentId,
          productId: productId,
          subscriptionId: subscriptionId || null,
          status: "ACTIVE",
          maxDevices: MAX_DEVICES_PER_KEY,
          activatedDevices: [],
          createdAt: Date.now()
        };

        if (subscriptionId) {
          const pending = await env.FATHOM_LICENSES.get(`SUBSCRIPTION_PENDING:${subscriptionId}`, { type: "json" });
          if (pending) {
            applySubscriptionUpdate(licenseRecord, pending, "subscription.pending");
            await env.FATHOM_LICENSES.delete(`SUBSCRIPTION_PENDING:${subscriptionId}`);
          }
          await env.FATHOM_LICENSES.put(`SUB:${subscriptionId}`, licenseKey);
        }

        await env.FATHOM_LICENSES.put(licenseKey, JSON.stringify(licenseRecord));
        await env.FATHOM_LICENSES.put(`ORDER:${paymentId}`, licenseKey);
        if (customerEmail) {
          await env.FATHOM_LICENSES.put(`EMAIL:${customerEmail}`, licenseKey);
        }

        return new Response(JSON.stringify({
          success: true,
          licenseKey: licenseKey,
          tier: mapped.tier,
          deepLink: `fathom://billing/success?license_key=${licenseKey}`
        }), {
          headers: { "Content-Type": "application/json" }
        });

      } catch (err) {
        return new Response(JSON.stringify({ success: false, error: err.message }), {
          status: 500,
          headers: { "Content-Type": "application/json" }
        });
      }
    }

    return new Response(JSON.stringify({ error: "Endpoint not found" }), {
      status: 404,
      headers: { "Content-Type": "application/json" }
    });
  }
};
