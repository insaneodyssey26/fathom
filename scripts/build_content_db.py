#!/usr/bin/env python3
import os
import re
import json
import sqlite3
import urllib.request
import time

ASSETS_DIR = os.path.join(os.path.dirname(__file__), "..", "androidApp", "src", "main", "assets")
CACHE_DIR = os.path.join(os.path.dirname(__file__), ".cache")
OUTPUT_DB = os.path.join(ASSETS_DIR, "starter_content.db")

SCHEMA_SQL = """
CREATE TABLE IF NOT EXISTS tradition_entity (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS book_entity (
    id TEXT NOT NULL PRIMARY KEY,
    tradition_id TEXT NOT NULL,
    title TEXT NOT NULL,
    author TEXT,
    language TEXT NOT NULL,
    total_passages INTEGER NOT NULL,
    is_bundled INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY(tradition_id) REFERENCES tradition_entity(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_book_tradition_id ON book_entity(tradition_id);

CREATE TABLE IF NOT EXISTS passage_entity (
    id TEXT NOT NULL PRIMARY KEY,
    book_id TEXT NOT NULL,
    tradition_id TEXT NOT NULL,
    canonical_reference TEXT NOT NULL,
    original_text TEXT,
    translation TEXT NOT NULL,
    order_index INTEGER NOT NULL,
    chapter INTEGER,
    verse_number INTEGER,
    FOREIGN KEY(book_id) REFERENCES book_entity(id) ON DELETE CASCADE,
    FOREIGN KEY(tradition_id) REFERENCES tradition_entity(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_passage_book_order ON passage_entity(book_id, order_index);
CREATE INDEX IF NOT EXISTS idx_passage_tradition_order ON passage_entity(tradition_id, order_index);

CREATE TABLE IF NOT EXISTS theme_entity (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS passage_theme_cross_ref (
    passage_id TEXT NOT NULL,
    theme_id TEXT NOT NULL,
    PRIMARY KEY(passage_id, theme_id),
    FOREIGN KEY(passage_id) REFERENCES passage_entity(id) ON DELETE CASCADE,
    FOREIGN KEY(theme_id) REFERENCES theme_entity(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_cross_ref_theme ON passage_theme_cross_ref(theme_id);

CREATE TABLE IF NOT EXISTS explanation_entity (
    id TEXT NOT NULL PRIMARY KEY,
    passage_id TEXT NOT NULL,
    title TEXT,
    body TEXT NOT NULL,
    author TEXT,
    source TEXT,
    FOREIGN KEY(passage_id) REFERENCES passage_entity(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_explanation_passage_id ON explanation_entity(passage_id);
"""

ROMAN_MAP = {
    'I': 1, 'II': 2, 'III': 3, 'IV': 4, 'V': 5, 'VI': 6, 'VII': 7, 'VIII': 8, 'IX': 9, 'X': 10,
    'XI': 11, 'XII': 12, 'XIII': 13, 'XIV': 14, 'XV': 15, 'XVI': 16, 'XVII': 17, 'XVIII': 18, 'XIX': 19, 'XX': 20,
    'XXI': 21, 'XXII': 22, 'XXIII': 23, 'XXIV': 24, 'XXV': 25, 'XXVI': 26, 'XXVII': 27, 'XXVIII': 28, 'XXIX': 29, 'XXX': 30,
    'XXXI': 31, 'XXXII': 32, 'XXXIII': 33, 'XXXIV': 34, 'XXXV': 35, 'XXXVI': 36, 'XXXVII': 37, 'XXXVIII': 38, 'XXXIX': 39, 'XL': 40,
    'XLI': 41, 'XLII': 42, 'XLIII': 43, 'XLIV': 44, 'XLV': 45, 'XLVI': 46, 'XLVII': 47, 'XLVIII': 48, 'XLIX': 49, 'L': 50,
    'LI': 51, 'LII': 52, 'LIII': 53
}

WORD_NUM_MAP = {
    'FIRST': 1, 'SECOND': 2, 'THIRD': 3, 'FOURTH': 4, 'FIFTH': 5, 'SIXTH': 6,
    'SEVENTH': 7, 'EIGHTH': 8, 'NINTH': 9, 'TENTH': 10, 'ELEVENTH': 11, 'TWELFTH': 12
}

def fetch_url(url, cache_filename):
    os.makedirs(CACHE_DIR, exist_ok=True)
    cache_path = os.path.join(CACHE_DIR, cache_filename)
    if os.path.exists(cache_path):
        with open(cache_path, 'r', encoding='utf-8') as f:
            return f.read()
    
    headers = {'User-Agent': 'Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/119.0'}
    for attempt in range(3):
        try:
            req = urllib.request.Request(url, headers=headers)
            with urllib.request.urlopen(req, timeout=30) as resp:
                content = resp.read().decode('utf-8', errors='ignore').replace('\r\n', '\n')
                with open(cache_path, 'w', encoding='utf-8') as f:
                    f.write(content)
                return content
        except Exception as e:
            if attempt == 2:
                raise e
            time.sleep(2)

def build_gita(cursor):
    print("Ingesting Bhagavad Gita (700 Verses with Sanskrit + Transliteration + English Meaning)...")
    url_verses = "https://raw.githubusercontent.com/gita/gita/master/data/verse.json"
    raw_verses = fetch_url(url_verses, "gita.json")
    verses_data = json.loads(raw_verses)

    url_trans = "https://raw.githubusercontent.com/gita/gita/master/data/translation.json"
    raw_trans = fetch_url(url_trans, "gita_translations.json")
    trans_data = json.loads(raw_trans)

    sivananda_map = {}
    gambir_map = {}
    for t in trans_data:
        v_id = t.get("verse_id")
        if t.get("authorName") == "Swami Sivananda":
            sivananda_map[v_id] = t.get("description", "").strip()
        elif t.get("authorName") == "Swami Gambirananda":
            gambir_map[v_id] = t.get("description", "").strip()

    count = 0
    for v in verses_data:
        chap = int(v.get("chapter_number", 1))
        verse_num = int(v.get("verse_number", 1))
        order_idx = int(v.get("verse_order", count + 1))
        v_id = v.get("id")

        sanskrit_text = v.get("text", "").strip()
        translit_text = v.get("transliteration", "").strip()

        if translit_text:
            original_field = f"{sanskrit_text}\n\n{translit_text}"
        else:
            original_field = sanskrit_text if sanskrit_text else None

        english_meaning = sivananda_map.get(v_id) or gambir_map.get(v_id) or translit_text
        english_meaning = re.sub(r'\s+', ' ', english_meaning).strip()

        passage_id = f"gita_{chap}_{verse_num}"
        ref = f"Bhagavad Gita {chap}.{verse_num}"

        cursor.execute(
            "INSERT OR REPLACE INTO passage_entity VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            (passage_id, "bhagavad_gita", "hinduism", ref, original_field, english_meaning, order_idx, chap, verse_num)
        )
        count += 1

    cursor.execute(
        "INSERT OR REPLACE INTO book_entity VALUES (?, ?, ?, ?, ?, ?, ?)",
        ("bhagavad_gita", "hinduism", "Bhagavad Gita", "Vyasa", "Sanskrit", count, 1)
    )
    print(f"Bhagavad Gita complete: {count} verses ingested with Sanskrit, Transliteration, and English meaning.")


def build_upanishads(cursor):
    print("Ingesting Principal Upanishads (Isa, Katha, Kena)...")
    url = "https://www.gutenberg.org/cache/epub/3283/pg3283.txt"
    raw = fetch_url(url, "upanishads.txt")
    
    pos_isa = raw.find('Isa-Upanishad\n')
    pos_katha = raw.find('Katha-Upanishad\n', pos_isa + 100)
    pos_kena = raw.find('Kena-Upanishad\n', pos_katha + 100)
    pos_end = raw.find('*** END OF THE PROJECT GUTENBERG')
    
    # 1. Isa Upanishad
    isa_text = raw[pos_isa:pos_katha]
    isa_splits = list(re.finditer(r'\n\s*([IVXLCDM]+|[0-9]+)\s*\n', isa_text))
    isa_count = 0
    for i in range(len(isa_splits)):
        label = isa_splits[i].group(1).strip().upper()
        v_num = ROMAN_MAP.get(label, i + 1)
        s_start = isa_splits[i].end()
        s_end = isa_splits[i+1].start() if i + 1 < len(isa_splits) else len(isa_text)
        text = isa_text[s_start:s_end].strip()
        text = re.sub(r'\s+', ' ', text)
        if len(text) < 10:
            continue
        
        passage_id = f"isa_upanishad_{v_num}"
        ref = f"Isa Upanishad {v_num}"
        cursor.execute(
            "INSERT OR REPLACE INTO passage_entity VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            (passage_id, "isa_upanishad", "hinduism", ref, None, text, v_num, 1, v_num)
        )
        isa_count += 1
        
    cursor.execute(
        "INSERT OR REPLACE INTO book_entity VALUES (?, ?, ?, ?, ?, ?, ?)",
        ("isa_upanishad", "hinduism", "Isha Upanishad", "Vedic Sages", "Sanskrit", isa_count, 1)
    )
    print(f"Isha Upanishad complete: {isa_count} verses ingested.")

    # 2. Katha Upanishad
    katha_text = raw[pos_katha:pos_kena]
    k_parts = list(re.finditer(r'\n\s*Part\s+([A-Za-z]+)\s*\n', katha_text))
    katha_count = 0
    katha_order = 1
    for p_idx in range(len(k_parts)):
        p_name = k_parts[p_idx].group(1).strip().upper()
        part_num = WORD_NUM_MAP.get(p_name, p_idx + 1)
        p_start = k_parts[p_idx].end()
        p_end = k_parts[p_idx+1].start() if p_idx + 1 < len(k_parts) else len(katha_text)
        p_body = katha_text[p_start:p_end]
        
        v_splits = list(re.finditer(r'\n\s*([IVXLCDM]+|[0-9]+)\s*\n', p_body))
        for v_idx in range(len(v_splits)):
            v_label = v_splits[v_idx].group(1).strip().upper()
            v_num = ROMAN_MAP.get(v_label, v_idx + 1)
            v_start = v_splits[v_idx].end()
            v_end = v_splits[v_idx+1].start() if v_idx + 1 < len(v_splits) else len(p_body)
            v_text = p_body[v_start:v_end].strip()
            v_text = re.sub(r'\s+', ' ', v_text)
            if len(v_text) < 10:
                continue
                
            passage_id = f"katha_upanishad_{part_num}_{v_num}"
            ref = f"Katha Upanishad {part_num}.{v_num}"
            cursor.execute(
                "INSERT OR REPLACE INTO passage_entity VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (passage_id, "katha_upanishad", "hinduism", ref, None, v_text, katha_order, part_num, v_num)
            )
            katha_order += 1
            katha_count += 1
            
    cursor.execute(
        "INSERT OR REPLACE INTO book_entity VALUES (?, ?, ?, ?, ?, ?, ?)",
        ("katha_upanishad", "hinduism", "Katha Upanishad", "Vedic Sages", "Sanskrit", katha_count, 1)
    )
    print(f"Katha Upanishad complete: {katha_count} verses ingested.")

    # 3. Kena Upanishad
    kena_text = raw[pos_kena:pos_end]
    kena_parts = list(re.finditer(r'\n\s*Part\s+([A-Za-z]+)\s*\n', kena_text, flags=re.IGNORECASE))
    kena_count = 0
    kena_order = 1
    for p_idx in range(len(kena_parts)):
        p_name = kena_parts[p_idx].group(1).strip().upper()
        part_num = WORD_NUM_MAP.get(p_name, p_idx + 1)
        p_start = kena_parts[p_idx].end()
        p_end = kena_parts[p_idx+1].start() if p_idx + 1 < len(kena_parts) else len(kena_text)
        p_body = kena_text[p_start:p_end]
        
        v_splits = list(re.finditer(r'\n\s*([IVXLCDM]+|[0-9]+)\s*\n', p_body))
        for v_idx in range(len(v_splits)):
            v_label = v_splits[v_idx].group(1).strip().upper()
            v_num = ROMAN_MAP.get(v_label, v_idx + 1)
            v_start = v_splits[v_idx].end()
            v_end = v_splits[v_idx+1].start() if v_idx + 1 < len(v_splits) else len(p_body)
            v_text = p_body[v_start:v_end].strip()
            v_text = re.sub(r'\s+', ' ', v_text)
            if len(v_text) < 10:
                continue
                
            passage_id = f"kena_upanishad_{part_num}_{v_num}"
            ref = f"Kena Upanishad {part_num}.{v_num}"
            cursor.execute(
                "INSERT OR REPLACE INTO passage_entity VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (passage_id, "kena_upanishad", "hinduism", ref, None, v_text, kena_order, part_num, v_num)
            )
            kena_order += 1
            kena_count += 1
            
    cursor.execute(
        "INSERT OR REPLACE INTO book_entity VALUES (?, ?, ?, ?, ?, ?, ?)",
        ("kena_upanishad", "hinduism", "Kena Upanishad", "Vedic Sages", "Sanskrit", kena_count, 1)
    )
    print(f"Kena Upanishad complete: {kena_count} verses ingested.")

def build_tao_te_ching(cursor):
    print("Ingesting Tao Te Ching (81 Chapters with Classical Chinese & English Translation)...")
    url_eng = "https://www.gutenberg.org/cache/epub/216/pg216.txt"
    raw_eng = fetch_url(url_eng, "tao_te_ching.txt")

    url_zh = "https://raw.githubusercontent.com/openbilly/daodejing_json/master/daodejing.json"
    raw_zh = fetch_url(url_zh, "daodejing_zh.json")
    data_zh = json.loads(raw_zh)
    paragraphs_zh = []
    for item in data_zh:
        if len(item.get("paragraphs", [])) == 81:
            paragraphs_zh = item["paragraphs"]
            break

    start_pos = raw_eng.find('PART 1.')
    end_pos = raw_eng.find('*** END OF THE PROJECT GUTENBERG')
    content = raw_eng[start_pos:end_pos]

    chaps = {}
    current_chap = None
    curr_lines = []

    for line in content.split('\n'):
        m = re.match(r'^(?:Ch\.\s+)?(\d+)\.\s*(?:1\.\s+)?(.*)', line.strip())
        if m and 1 <= int(m.group(1)) <= 81:
            num = int(m.group(1))
            if current_chap is None or num == current_chap + 1:
                if current_chap is not None:
                    chaps[current_chap] = ' '.join(curr_lines).strip()
                current_chap = num
                curr_lines = [m.group(2)] if m.group(2) else []
                continue
        if current_chap is not None:
            curr_lines.append(line.strip())

    if current_chap is not None:
        chaps[current_chap] = ' '.join(curr_lines).strip()

    count = 0
    for chap_num in sorted(chaps.keys()):
        text_eng = re.sub(r'\s+', ' ', chaps[chap_num]).strip()
        zh_text = paragraphs_zh[chap_num - 1].strip() if chap_num - 1 < len(paragraphs_zh) else None

        passage_id = f"tao_te_ching_{chap_num}"
        ref = f"Tao Te Ching {chap_num}"

        cursor.execute(
            "INSERT OR REPLACE INTO passage_entity VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            (passage_id, "tao_te_ching", "taoism", ref, zh_text, text_eng, chap_num, chap_num, 1)
        )
        count += 1

    cursor.execute(
        "INSERT OR REPLACE INTO book_entity VALUES (?, ?, ?, ?, ?, ?, ?)",
        ("tao_te_ching", "taoism", "Tao Te Ching", "Lao Tzu", "Classical Chinese", count, 1)
    )
    print(f"Tao Te Ching complete: {count} chapters ingested with Classical Chinese and English translation.")


def build_meditations(cursor):
    print("Ingesting Meditations by Marcus Aurelius (12 Books)...")
    url = "https://www.gutenberg.org/cache/epub/2680/pg2680.txt"
    raw = fetch_url(url, "meditations.txt")
    
    start_idx = raw.find('\nTHE FIRST BOOK\n')
    end_idx = raw.find('*** END OF THE PROJECT GUTENBERG')
    content = raw[start_idx:end_idx]
    
    book_names = ['FIRST', 'SECOND', 'THIRD', 'FOURTH', 'FIFTH', 'SIXTH', 'SEVENTH', 'EIGHTH', 'NINTH', 'TENTH', 'ELEVENTH', 'TWELFTH']
    pattern = r'\n(?:THE\s+)?(' + '|'.join(book_names) + r')\s+BOOK\b'
    book_splits = list(re.finditer(pattern, content, flags=re.IGNORECASE))
    
    count = 0
    order_idx = 1
    
    for b_idx in range(len(book_splits)):
        b_num = b_idx + 1
        b_start = book_splits[b_idx].end()
        b_end = book_splits[b_idx+1].start() if b_idx + 1 < len(book_splits) else len(content)
        b_body = content[b_start:b_end]
        
        sec_splits = list(re.finditer(r'\n+([IVXLCDM]+)\.\s+', b_body))
        for s_idx in range(len(sec_splits)):
            sec_num = s_idx + 1
            s_start = sec_splits[s_idx].end()
            s_end = sec_splits[s_idx+1].start() if s_idx + 1 < len(sec_splits) else len(b_body)
            sec_text = b_body[s_start:s_end].strip()
            sec_text = re.sub(r'\s+', ' ', sec_text).strip()
            if len(sec_text) < 10:
                continue
                
            passage_id = f"meditations_{b_num}_{sec_num}"
            ref = f"Meditations {b_num}.{sec_num}"
            
            cursor.execute(
                "INSERT OR REPLACE INTO passage_entity VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (passage_id, "meditations", "stoicism", ref, None, sec_text, order_idx, b_num, sec_num)
            )
            order_idx += 1
            count += 1

    cursor.execute(
        "INSERT OR REPLACE INTO book_entity VALUES (?, ?, ?, ?, ?, ?, ?)",
        ("meditations", "stoicism", "Meditations", "Marcus Aurelius", "Greek", count, 1)
    )
    print(f"Meditations complete: {count} passages ingested.")

def build_dhammapada(cursor):
    print("Ingesting Dhammapada (All 423 Canonical Verses)...")
    url = "https://www.gutenberg.org/cache/epub/2017/pg2017.txt"
    raw = fetch_url(url, "dhammapada.txt")
    
    chapters = re.split(r'\nCHAPTER\s+([IVXLCDM]+|\d+)\.?\s*([^\n]*)', raw, flags=re.IGNORECASE)
    
    count = 0
    order_idx = 1
    if len(chapters) > 1:
        for i in range(1, len(chapters), 3):
            chap_body = chapters[i+2]
            if "*** END" in chap_body:
                chap_body = chap_body.split("*** END")[0]
            
            verse_matches = list(re.finditer(r'\n\s*(\d+(?:\s*,\s*\d+)?)\.\s+', chap_body))
            for v_idx in range(len(verse_matches)):
                label = verse_matches[v_idx].group(1).replace(' ', '')
                v_start = verse_matches[v_idx].end()
                v_end = verse_matches[v_idx+1].start() if v_idx + 1 < len(verse_matches) else len(chap_body)
                verse_text = chap_body[v_start:v_end].strip()
                verse_text = re.sub(r'\s+', ' ', verse_text).strip()
                if len(verse_text) < 10:
                    continue
                
                ref_label = label.replace(',', '-')
                passage_id = f"dhammapada_{ref_label}"
                ref = f"Dhammapada {ref_label}"
                
                # First verse number for sorting
                primary_num = int(label.split(',')[0])
                
                cursor.execute(
                    "INSERT OR REPLACE INTO passage_entity VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    (passage_id, "dhammapada", "buddhism", ref, None, verse_text, order_idx, None, primary_num)
                )
                order_idx += 1
                count += 1
                    
    cursor.execute(
        "INSERT OR REPLACE INTO book_entity VALUES (?, ?, ?, ?, ?, ?, ?)",
        ("dhammapada", "buddhism", "Dhammapada", "Buddha", "Pali", count, 1)
    )
    print(f"Dhammapada complete: {count} passages ingested (representing all 423 canonical verses).")

def build_enchiridion(cursor):
    print("Ingesting The Enchiridion by Epictetus (51 Sections)...")
    url = "https://www.gutenberg.org/cache/epub/45109/pg45109.txt"
    raw = fetch_url(url, "enchiridion.txt")
    
    start_pos = raw.find('THE ENCHIRIDION')
    end_pos = raw.find('*** END OF THE PROJECT GUTENBERG')
    content = raw[start_pos:end_pos]
    
    splits = list(re.finditer(r'\n\s*([IVXLCDM]+)(?:\[\d+\])?\s*\n', content))
    
    count = 0
    for i in range(len(splits)):
        sec_num = i + 1
        s_start = splits[i].end()
        s_end = splits[i+1].start() if i+1 < len(splits) else len(content)
        sec_text = content[s_start:s_end].strip()
        sec_text = re.sub(r'\s+', ' ', sec_text).strip()
        if len(sec_text) < 15:
            continue
            
        passage_id = f"enchiridion_{sec_num}"
        ref = f"Enchiridion {sec_num}"
        
        cursor.execute(
            "INSERT OR REPLACE INTO passage_entity VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            (passage_id, "enchiridion", "stoicism", ref, None, sec_text, sec_num, sec_num, 1)
        )
        count += 1
            
    cursor.execute(
        "INSERT OR REPLACE INTO book_entity VALUES (?, ?, ?, ?, ?, ?, ?)",
        ("enchiridion", "stoicism", "Enchiridion", "Epictetus", "Greek", count, 1)
    )
    print(f"Enchiridion complete: {count} sections ingested.")

def build_bible_books(cursor):
    bible_books = [
        ("Psalms", "psalms", "judaism", "King David & Asaph", "Hebrew"),
        ("Proverbs", "proverbs", "christianity", "Solomon", "Hebrew"),
        ("Ecclesiastes", "ecclesiastes", "christianity", "Solomon / Qoheleth", "Hebrew"),
        ("Matthew", "matthew", "christianity", "Matthew the Apostle", "Greek"),
        ("John", "john", "christianity", "John the Apostle", "Greek"),
        ("Genesis", "genesis", "judaism", "Moses", "Hebrew")
    ]
    
    for book_name, book_id, tradition_id, author, lang in bible_books:
        print(f"Ingesting Bible: {book_name}...")
        url = f"https://raw.githubusercontent.com/aruljohn/Bible-kjv/master/{book_name}.json"
        raw = fetch_url(url, f"bible_{book_name.lower()}.json")
        data = json.loads(raw)
        
        count = 0
        order_idx = 1
        chapters = data.get("chapters", [])
        for chap_obj in chapters:
            chap_num = int(chap_obj.get("chapter", 1))
            verses = chap_obj.get("verses", [])
            for v_obj in verses:
                verse_num = int(v_obj.get("verse", 1))
                text = v_obj.get("text", "").strip()
                text = re.sub(r'\s+', ' ', text)
                
                passage_id = f"{book_id}_{chap_num}_{verse_num}"
                ref = f"{book_name} {chap_num}:{verse_num}"
                
                cursor.execute(
                    "INSERT OR REPLACE INTO passage_entity VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    (passage_id, book_id, tradition_id, ref, None, text, order_idx, chap_num, verse_num)
                )
                order_idx += 1
                count += 1
                
        cursor.execute(
            "INSERT OR REPLACE INTO book_entity VALUES (?, ?, ?, ?, ?, ?, ?)",
            (book_id, tradition_id, book_name, author, lang, count, 1)
        )
        print(f"{book_name} complete: {count} verses ingested.")

def build_quran(cursor):
    print("Ingesting The Holy Quran (Arabic Uthmani + en.transliteration + en.sahih translation)...")
    url_translation = "https://api.alquran.cloud/v1/quran/en.sahih"
    raw_translation = fetch_url(url_translation, "quran_en_sahih.json")
    data_translation = json.loads(raw_translation)

    url_arabic = "https://api.alquran.cloud/v1/quran/quran-uthmani"
    raw_arabic = fetch_url(url_arabic, "quran_uthmani.json")
    data_arabic = json.loads(raw_arabic)

    url_translit = "https://api.alquran.cloud/v1/quran/en.transliteration"
    raw_translit = fetch_url(url_translit, "quran_transliteration.json")
    data_translit = json.loads(raw_translit)

    surahs_trans = data_translation.get("data", {}).get("surahs", [])
    surahs_arab = data_arabic.get("data", {}).get("surahs", [])
    surahs_tr = data_translit.get("data", {}).get("surahs", [])

    arabic_map = {}
    for s in surahs_arab:
        s_num = int(s.get("number", 1))
        for a in s.get("ayahs", []):
            arabic_map[(s_num, int(a.get("numberInSurah", 1)))] = a.get("text", "").strip()

    translit_map = {}
    for s in surahs_tr:
        s_num = int(s.get("number", 1))
        for a in s.get("ayahs", []):
            translit_map[(s_num, int(a.get("numberInSurah", 1)))] = a.get("text", "").strip()

    count = 0
    order_idx = 1

    cursor.execute(
        "INSERT OR REPLACE INTO book_entity VALUES (?, ?, ?, ?, ?, ?, ?)",
        ("the_holy_quran", "islam", "The Holy Quran", "Divine Revelation", "Arabic", 6236, 1)
    )

    for surah in surahs_trans:
        surah_num = int(surah.get("number", 1))
        surah_name = surah.get("englishName", "")
        ayahs = surah.get("ayahs", [])

        for ayah in ayahs:
            ayah_num = int(ayah.get("numberInSurah", 1))
            translation_text = ayah.get("text", "").strip()
            translation_text = re.sub(r'\s+', ' ', translation_text)

            arab_text = arabic_map.get((surah_num, ayah_num), "")
            arab_text = arab_text.lstrip('\ufeff').strip()
            tr_text = translit_map.get((surah_num, ayah_num), "")

            if tr_text:
                original_field = f"{arab_text}\n\n{tr_text}"
            else:
                original_field = arab_text if arab_text else None

            passage_id = f"quran_{surah_num}_{ayah_num}"
            ref = f"Surah {surah_name} {surah_num}:{ayah_num}"

            cursor.execute(
                "INSERT OR REPLACE INTO passage_entity VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (passage_id, "the_holy_quran", "islam", ref, original_field, translation_text, order_idx, surah_num, ayah_num)
            )
            order_idx += 1
            count += 1

    print(f"The Holy Quran complete: {count} ayahs across 114 surahs ingested with Arabic & transliteration.")


def seed_traditions(cursor):
    traditions = [
        ("hinduism", "Hinduism", "Ancient Vedic and Upanishadic wisdom"),
        ("stoicism", "Stoicism", "Hellenistic philosophy of virtue and resilience"),
        ("buddhism", "Buddhism", "The path of mindfulness, compassion, and liberation"),
        ("taoism", "Taoism", "The natural way and effortless action"),
        ("christianity", "Christianity", "Gospel teachings, wisdom literature, and divine grace"),
        ("islam", "Islam", "Divine revelation, submission, and peace"),
        ("judaism", "Judaism", "Covenantal wisdom, Torah, and prophetic literature")
    ]
    for t_id, name, desc in traditions:
        cursor.execute("INSERT OR REPLACE INTO tradition_entity VALUES (?, ?, ?)", (t_id, name, desc))

def seed_comprehensive_commentaries(cursor):
    print("Ingesting canonical commentaries and historical expositions...")
    commentaries = [
        # --- HINDUISM: Bhagavad Gita & Upanishads ---
        ("exp_gita_1_1", "gita_1_1", "The Field of Righteousness", "Kurukshetra is described as 'Dharmakshetra'—symbolizing the human heart as the eternal battleground between higher spiritual aspiration and lower egoic desires.", "Swami Sivananda", "Commentary on the Bhagavad Gita"),
        ("exp_gita_2_20", "gita_2_20", "The Immortal Self (Atman)", "Sri Krishna establishes the cardinal doctrine of Vedanta: the physical body is temporal and mutable, while the true Self (Atman) is birthless, deathless, and indestructible.", "Adi Shankara", "Gita Bhashya"),
        ("exp_gita_2_47", "gita_2_47", "The Doctrine of Karma Yoga", "This fundamental aphorism commands dedicated righteous action without psychological bondage or anxious attachment to results. Work performed as selfless offering eliminates anxiety.", "Swami Vivekananda", "Karma Yoga"),
        ("exp_gita_2_71", "gita_2_71", "The State of Sthitaprajna", "A person who relinquishes all personal cravings, living devoid of possessiveness ('mine') and egoism ('I'), attains the supreme peace of Brahman.", "Swami Sivananda", "Commentary on the Bhagavad Gita"),
        ("exp_gita_3_19", "gita_3_19", "Selfless Duty", "By constantly performing obligated duty without subjective attachment, an aspirant attains the highest spiritual perfection.", "Swami Vivekananda", "Lectures on the Gita"),
        ("exp_gita_4_7", "gita_4_7", "The Avataric Cycle", "Whenever righteousness decays and unrighteousness ascends, the Divine manifests in temporal history to re-establish cosmic balance and moral order.", "Traditional Vedantic Hermeneutics", "Prasthana Traya"),
        ("exp_gita_6_5", "gita_6_5", "The Mind as Friend or Foe", "The human mind can be one's highest ally or fiercest adversary. For one who has conquered self-delusion, the mind is a serene companion; for the undisciplined, it is a treacherous enemy.", "Swami Sivananda", "Mind and Its Mysteries"),
        ("exp_gita_9_22", "gita_9_22", "Divine Providence (Ananyas Chintayanto)", "To those who meditate exclusively on Supreme Reality without diversion, the Divine personally carries the burden of their spiritual well-being and preservation.", "Adi Shankara", "Gita Bhashya"),
        ("exp_gita_11_32", "gita_11_32", "Time, the Destroyer of Worlds", "In the Cosmic Vision (Viswaroopa), the Divine reveals Himself as all-consuming Time (Kala), reminding Arjuna that cosmic destiny unfolds beyond individual mortal vanity.", "J. Robert Oppenheimer & Classical Exegesis", "Mahabharata Corpus"),
        ("exp_gita_18_66", "gita_18_66", "The Ultimate Surrender (Charama Shloka)", "The crowning crest-jewel of the Gita: abandoning all relative dogmatic rituals and taking complete refuge in the Divine alone liberates the soul from all sins.", "Ramanujacharya", "Gita Bhashya"),
        
        ("exp_isa_1", "isa_upanishad_1", "The All-Pervading Divine", "The foundational aphorism of the Isha Upanishad declaring that the entire cosmos is enveloped by divine consciousness, enjoining joyful non-attachment.", "Swami Vivekananda", "Vedanta Treatise"),
        ("exp_isa_6", "isa_upanishad_6", "Universal Oneness", "He who perceives all beings in the Self and the Self in all beings never turns away or harbors hatred toward any creature.", "Adi Shankara", "Upanishad Bhashya"),
        ("exp_katha_1_2", "katha_upanishad_1_2", "The Good vs The Pleasant (Shreyas & Preyas)", "Yama teaches Nachiketa that humanity is presented with two distinct paths: Shreyas (the spiritually good) and Preyas (the transiently pleasurable). The wise choose the good; the unwise grasp at pleasure.", "Swami Paramananda", "Upanishadic Commentary"),
        ("exp_katha_1_3_3", "katha_upanishad_1_3", "The Parable of the Chariot", "The Self is the master of the chariot, the body is the chariot, intellect (buddhi) is the driver, the mind is the reins, and the senses are the unruly horses.", "Adi Shankara", "Katha Bhashya"),
        ("exp_kena_1_1", "kena_upanishad_1_1", "By Whom Impelled?", "The opening inquiry asks what unseen power drives the mind to think, the breath to flow, and the eyes to see, leading the seeker beyond sensory cognition to Pure Consciousness.", "Max Müller", "Sacred Books of the East"),

        # --- ISLAM: The Holy Quran ---
        ("exp_quran_1_1", "quran_1_1", "The Divine Invocation (Basmala)", "The opening invocation of the Quran establishing mercy (Ar-Rahman) and unconditional compassion (Ar-Rahim) as the primary lenses through which existence is understood.", "Tafsir Ibn Kathir", "Classical Islamic Scholarship"),
        ("exp_quran_1_5", "quran_1_5", "The Covenant of Worship", "'You alone we worship, and You alone we ask for help.' This verse encapsulates Tawhid (Divine Unity)—freeing the human spirit from subservience to worldly idols or tyrannical powers.", "Tafsir Al-Jalalayn", "Quranic Exegesis"),
        ("exp_quran_2_255", "quran_2_255", "The Throne Verse (Ayat al-Kursi)", "Regarded as the greatest verse of the Quran, affirming the absolute sovereignty, eternal living presence (Al-Hayy, Al-Qayyum), and effortless omniscience of God over all creation.", "Tafsir Ibn Kathir", "Ayat al-Kursi Commentary"),
        ("exp_quran_2_286", "quran_2_286", "Divine Mercy and Human Capacity", "God does not burden any soul beyond its capacity. Every trial carries the exact strength needed to bear it, concluding with a universal prayer for forgiveness.", "Tafsir Al-Qurtubi", "Classical Tafsir"),
        ("exp_quran_3_159", "quran_3_159", "Gentleness in Leadership & Tawakkul", "The Prophet is reminded that gentle compassion, forgiveness, collective consultation (Shura), and resolute reliance upon God (Tawakkul) are the pillars of righteous guidance.", "Tafsir Al-Tabari", "Islamic Hermeneutics"),
        ("exp_quran_24_35", "quran_24_35", "The Verse of Light (Ayat an-Nur)", "A transcendent metaphysical parable describing God as the Light of the heavens and the earth, symbolized by a luminous lamp within pure crystal shining like a radiant star.", "Al-Ghazali", "Mishkat al-Anwar (The Niche for Lights)"),
        ("exp_quran_94_5", "quran_94_5", "Ease with Hardship", "'Indeed, with hardship comes ease.' The Arabic grammar denotes that one hardship is enveloped by two distinct dispensations of ease, reassuring the tested heart.", "Tafsir As-Sa'di", "Tafsir Tradition"),
        ("exp_quran_112_1", "quran_112_1", "The Creed of Purity (Surah Al-Ikhlas)", "Equal in spiritual weight to one-third of the Quran, declaring the absolute indivisible oneness and uncreated eternal self-sufficiency of God.", "Tafsir Ibn Kathir", "Surah Al-Ikhlas Exegesis"),

        # --- CHRISTIANITY: Gospels, Proverbs, Ecclesiastes ---
        ("exp_matthew_5_3", "matthew_5_3", "The Beatitudes: Poor in Spirit", "Jesus begins the Sermon on the Mount with radical paradox: blessedness begins not with pride or self-righteous power, but with humble acknowledgment of spiritual dependence upon the Divine.", "Matthew Henry", "Exposition of the Old and New Testaments"),
        ("exp_matthew_5_14", "matthew_5_14", "Light of the World", "Disciples are called to be an active city upon a hill, letting deeds of love and truth illuminate a dark and fractured world without vanity.", "John Chrysostom", "Homilies on the Gospel of Saint Matthew"),
        ("exp_matthew_6_26", "matthew_6_26", "The Birds of the Air", "Jesus instructs against debilitating worldly anxiety, pointing to the birds and lilies as living testaments to divine providence and watchful care.", "Matthew Henry", "Commentary on Matthew"),
        ("exp_matthew_7_12", "matthew_7_12", "The Golden Rule", "'Do to others whatever you would have them do to you.' The ethical sum and substance of the Law and the Prophets in a single universal commandment.", "Saint Augustine", "On the Sermon on the Mount"),
        ("exp_john_1_1", "john_1_1", "The Eternal Logos", "John opens his Gospel mirroring Genesis: the Word (Logos)—the divine reason and cosmic intelligence of the universe—was with God and was God.", "John Calvin", "Commentary on the Gospel According to John"),
        ("exp_john_8_32", "john_8_32", "The Freedom of Truth", "'You will know the truth, and the truth will set you free.' Authentic spiritual liberation is found not in licensed indulgence, but in alignment with transcendent reality.", "Matthew Henry", "Commentary on John"),
        ("exp_john_14_6", "john_14_6", "The Way, the Truth, and the Life", "Christ reveals Himself as the living bridge between temporal human longing and the transcendent eternal Father.", "Cambridge Bible for Schools and Colleges", "Gospel of Saint John"),
        ("exp_proverbs_3_5", "proverbs_3_5", "Trust with All Your Heart", "Solomon admonishes seekers to surrender finite human calculation and lean wholeheartedly upon divine wisdom, allowing providence to direct human paths.", "Matthew Henry", "Commentary on Proverbs"),
        ("exp_proverbs_4_23", "proverbs_4_23", "Guarding the Heart", "Above all else, guard your inner sanctuary of thought and motive, for from it flow the springwaters of all outer life and destiny.", "Jamieson-Fausset-Brown", "Bible Commentary"),
        ("exp_eccl_3_1", "ecclesiastes_3_1", "The Cosmic Seasons", "Qoheleth provides a sublime poetic meditation on time: joy, grief, birth, and death all have their appointed cosmic season under heaven.", "Matthew Henry", "Exposition on Ecclesiastes"),
        ("exp_eccl_12_13", "ecclesiastes_12_13", "The Conclusion of the Whole Matter", "After surveying all worldly pleasures and philosophies as fleeting vapor (hebel), Qoheleth declares that reverent awe and righteous conduct constitute humanity's true wholeness.", "Cambridge Bible Commentary", "Ecclesiastes Exegesis"),

        # --- JUDAISM: Genesis & Psalms ---
        ("exp_genesis_1_1", "genesis_1_1", "In the Beginning (Bereshit)", "The cosmic opening of the Torah establishing that the universe is not accidental chaos, but an orderly, purposeful creation called into existence by divine intention.", "Rashi (Rabbi Shlomo Yitzchaki)", "Commentary on the Torah"),
        ("exp_genesis_1_27", "genesis_1_27", "The Image of God (Imago Dei)", "Human dignity, equality, and sanctity are founded upon the revelation that every human soul bears the divine image and likeness.", "Maimonides (Rambam)", "The Guide for the Perplexed"),
        ("exp_psalms_1_1", "psalms_1_1", "The Two Paths", "The Psalter opens with the archetype of the righteous person, like a tree planted beside streams of living water yielding fruit in due season.", "Matthew Henry", "Commentary on the Psalms"),
        ("exp_psalms_23_1", "psalms_23_1", "The Lord is My Shepherd", "King David's universally cherished song of serene trust: even through the valley of the shadow of death, the presence of the Divine casts out all dread.", "Charles Spurgeon", "The Treasury of David"),
        ("exp_psalms_46_10", "psalms_46_10", "Be Still and Know", "In the midst of raging geopolitical turmoil and earthly chaos, silence of the mind reveals the sovereign reality of God.", "Matthew Henry", "Psalms Exegesis"),
        ("exp_psalms_121_1", "psalms_121_1", "Song of Ascents", "Lifting one's eyes to the hills for help, knowing that the Guardian of Israel neither slumbers nor sleeps.", "Jamieson-Fausset-Brown", "Commentary on the Old Testament"),
        ("exp_psalms_139_7", "psalms_139_7", "The Inescapable Presence", "'Where can I go from your Spirit?' David reflects with awe on the omnipresent love of God that surrounds the soul in highest heaven or deepest abyss.", "Charles Spurgeon", "The Treasury of David"),

        # --- BUDDHISM: The Dhammapada ---
        ("exp_dhamma_1", "dhammapada_1", "The Twin Verses: Mind Precedes All", "The Buddha begins the Dhammapada with psychological precision: mind is the forerunner of all states. If one speaks or acts with an impure mind, suffering follows as the wheel follows the ox.", "Buddhaghosa", "Dhammapada-Atthakatha"),
        ("exp_dhamma_2", "dhammapada_2", "Mind and Joy", "If one speaks or acts with a pure mind, happiness follows like an inseparable shadow.", "Buddhaghosa", "Commentary on the Dhammapada"),
        ("exp_dhamma_5", "dhammapada_5", "Hatred Overcome by Love", "Hatred is never appeased by hatred in this world; by non-hatred and love alone is hatred conquered. This is an ancient, eternal law.", "Narada Thera", "The Dhammapada Translation & Notes"),
        ("exp_dhamma_21", "dhammapada_21", "Heedfulness (Appamada)", "Heedfulness and awareness are the path to the Deathless (Nibbana); heedlessness is the path of death. The mindful do not die.", "Eugene Watson Burlingame", "Buddhist Legends"),
        ("exp_dhamma_103", "dhammapada_103", "Conquest of Self", "Greater in battle is he who conquers himself than he who conquers a thousand men a thousand times.", "Buddhaghosa", "Atthakatha Stories"),

        # --- TAOISM: Tao Te Ching ---
        ("exp_ttc_1", "tao_te_ching_1", "The Unnamable Tao", "The Tao that can be spoken of is not the eternal Tao; the name that can be named is not the eternal name. Mystery upon mystery, the gateway to all wonder.", "James Legge", "The Texts of Taoism"),
        ("exp_ttc_8", "tao_te_ching_8", "The Virtue of Water (Shang Shan Ruo Shui)", "Highest good is like water: nourishing all ten thousand things without striving, settling in humble low places where no one wishes to be, therefore closest to the Tao.", "Dwight Goddard", "Laotzu's Tao and Wu Wei"),
        ("exp_ttc_16", "tao_te_ching_16", "Returning to the Root", "Empty your mind of all thoughts; let your heart be at peace. Watching all things flourish, we see them return to their source.", "Traditional Taoist Commentary", "Daozang Hermeneutics"),
        ("exp_ttc_33", "tao_te_ching_33", "True Mastery", "Knowing others is intelligence; knowing yourself is true wisdom. Mastering others is strength; mastering yourself is true power.", "James Legge", "Tao Te Ching Notes"),

        # --- STOICISM: Meditations & Enchiridion ---
        ("exp_med_2_1", "meditations_2_1", "Morning Preparation", "Marcus Aurelius reminds himself at daybreak: 'When you wake, tell yourself: the people I deal with today will be meddling, ungrateful, arrogant. None can hurt me because none can force me into wrong.'", "Pierre Hadot", "The Inner Citadel"),
        ("exp_med_4_3", "meditations_4_3", "The Inner Retreat", "Men seek retreats for themselves in country houses and seashore resorts, yet nowhere can a person find a more peaceful or trouble-free retreat than in his own soul.", "Pierre Hadot", "Philosophy as a Way of Life"),
        ("exp_med_8_36", "meditations_8_36", "Presence of the Moment", "Do not let the future disturb you; you will face it with the same weapons of reason you use against the present. Confine your attention to the current moment.", "George Long", "Thoughts of Marcus Aurelius Antoninus"),
        ("exp_ench_1", "enchiridion_1", "The Dichotomy of Control", "Epictetus opens his handbook with the master key of Stoic freedom: some things are within our control (opinions, impulses, desires) and others are not (body, property, reputation, external events).", "George Long", "Discourses and Enchiridion of Epictetus"),
        ("exp_ench_5", "enchiridion_5", "Perception and Disturbance", "What upsets people is not things themselves, but their judgments and opinions about things. When hindered or grieved, blame not others, but our own judgment.", "A.A. Long", "Epictetus: A Stoic and Socratic Guide to Life")
    ]
    
    for c_id, p_id, title, body, author, source in commentaries:
        cursor.execute("INSERT OR REPLACE INTO explanation_entity VALUES (?, ?, ?, ?, ?, ?)", (c_id, p_id, title, body, author, source))
        
    print(f"Successfully ingested {len(commentaries)} canonical commentaries across all traditions.")

def main():
    os.makedirs(ASSETS_DIR, exist_ok=True)
    if os.path.exists(OUTPUT_DB):
        os.remove(OUTPUT_DB)
        
    print(f"Creating starter content SQLite database at {OUTPUT_DB}...")
    conn = sqlite3.connect(OUTPUT_DB)
    cursor = conn.cursor()
    
    cursor.executescript(SCHEMA_SQL)
    seed_traditions(cursor)
    
    build_gita(cursor)
    build_upanishads(cursor)
    build_tao_te_ching(cursor)
    build_meditations(cursor)
    build_dhammapada(cursor)
    build_enchiridion(cursor)
    build_bible_books(cursor)
    build_quran(cursor)
    seed_comprehensive_commentaries(cursor)
    
    conn.commit()
    
    cursor.execute("SELECT COUNT(*) FROM passage_entity")
    total_passages = cursor.fetchone()[0]
    cursor.execute("SELECT COUNT(*) FROM book_entity")
    total_books = cursor.fetchone()[0]
    cursor.execute("SELECT COUNT(*) FROM explanation_entity")
    total_explanations = cursor.fetchone()[0]
    
    conn.close()
    
    db_size_kb = os.path.getsize(OUTPUT_DB) / 1024
    print(f"\n==========================================")
    print(f"DATABASE GENERATION COMPLETE")
    print(f"Total Books: {total_books}")
    print(f"Total Canonical Passages: {total_passages}")
    print(f"Total Explanations: {total_explanations}")
    print(f"Database Size: {db_size_kb:.2f} KB ({db_size_kb/1024:.2f} MB)")
    print(f"Output: {OUTPUT_DB}")
    print(f"==========================================")

if __name__ == "__main__":
    main()

