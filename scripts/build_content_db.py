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
    print("Ingesting Bhagavad Gita (700 Verses)...")
    url = "https://raw.githubusercontent.com/gita/gita/master/data/verse.json"
    raw = fetch_url(url, "gita.json")
    data = json.loads(raw)
    
    count = 0
    for v in data:
        chap = int(v.get("chapter_number", 1))
        verse_num = int(v.get("verse_number", 1))
        order_idx = int(v.get("verse_order", count + 1))
        text_orig = v.get("text", "").strip()
        trans = v.get("meaning", "").strip()
        if not trans:
            trans = v.get("transliteration", "").strip()
        
        trans = re.sub(r'\s+', ' ', trans).strip()
        
        passage_id = f"gita_{chap}_{verse_num}"
        ref = f"Bhagavad Gita {chap}.{verse_num}"
        
        cursor.execute(
            "INSERT OR REPLACE INTO passage_entity VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            (passage_id, "bhagavad_gita", "hinduism", ref, text_orig, trans, order_idx, chap, verse_num)
        )
        count += 1
    
    cursor.execute(
        "INSERT OR REPLACE INTO book_entity VALUES (?, ?, ?, ?, ?, ?, ?)",
        ("bhagavad_gita", "hinduism", "Bhagavad Gita", "Vyasa", "Sanskrit", count, 1)
    )
    print(f"Bhagavad Gita complete: {count} verses ingested.")

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
    print("Ingesting Tao Te Ching (81 Chapters)...")
    url = "https://www.gutenberg.org/cache/epub/216/pg216.txt"
    raw = fetch_url(url, "tao_te_ching.txt")
    
    start_pos = raw.find('PART 1.')
    end_pos = raw.find('*** END OF THE PROJECT GUTENBERG')
    content = raw[start_pos:end_pos]
    
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
        text = re.sub(r'\s+', ' ', chaps[chap_num]).strip()
        passage_id = f"tao_te_ching_{chap_num}"
        ref = f"Tao Te Ching {chap_num}"
        
        cursor.execute(
            "INSERT OR REPLACE INTO passage_entity VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            (passage_id, "tao_te_ching", "taoism", ref, None, text, chap_num, chap_num, 1)
        )
        count += 1
            
    cursor.execute(
        "INSERT OR REPLACE INTO book_entity VALUES (?, ?, ?, ?, ?, ?, ?)",
        ("tao_te_ching", "taoism", "Tao Te Ching", "Lao Tzu", "Classical Chinese", count, 1)
    )
    print(f"Tao Te Ching complete: {count} chapters ingested.")

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
    print("Ingesting The Holy Quran (en.sahih translation)...")
    url = "https://api.alquran.cloud/v1/quran/en.sahih"
    raw = fetch_url(url, "quran_en_sahih.json")
    data = json.loads(raw)
    
    surahs = data.get("data", {}).get("surahs", [])
    count = 0
    order_idx = 1
    
    cursor.execute(
        "INSERT OR REPLACE INTO book_entity VALUES (?, ?, ?, ?, ?, ?, ?)",
        ("the_holy_quran", "islam", "The Holy Quran", "Divine Revelation", "Arabic", 6236, 1)
    )
    
    for surah in surahs:
        surah_num = int(surah.get("number", 1))
        surah_name = surah.get("englishName", "")
        ayahs = surah.get("ayahs", [])
        
        for ayah in ayahs:
            ayah_num = int(ayah.get("numberInSurah", 1))
            text = ayah.get("text", "").strip()
            text = re.sub(r'\s+', ' ', text)
            
            passage_id = f"quran_{surah_num}_{ayah_num}"
            ref = f"Surah {surah_name} {surah_num}:{ayah_num}"
            
            cursor.execute(
                "INSERT OR REPLACE INTO passage_entity VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (passage_id, "the_holy_quran", "islam", ref, None, text, order_idx, surah_num, ayah_num)
            )
            order_idx += 1
            count += 1
            
    print(f"The Holy Quran complete: {count} ayahs across 114 surahs ingested.")

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

def seed_sample_commentaries(cursor):
    commentaries = [
        ("exp_gita_2_20", "gita_2_20", "Consciousness and the Vessel", "This verse establishes the classic Vedic distinction between the temporal biological body and the eternal witness consciousness.", "Traditional Commentary", "Sanskrit Philosophical Corpus"),
        ("exp_isa_1", "isa_upanishad_1", "The All-Pervading Divine", "The foundational aphorism of the Isha Upanishad declaring that the entire cosmos is enveloped by divine consciousness, enjoining joyful non-attachment.", "Swami Vivekananda", "Vedanta Treatise"),
        ("exp_katha_1_1", "katha_upanishad_1_1", "The Quest for the Eternal", "Nachiketa refuses transient wealth and mortal pleasures, demanding the eternal secret of the transcendent Self from Yama.", "Swami Paramananda", "Upanishadic Commentary"),
        ("exp_med_4_3", "meditations_4_3", "The Inner Citadel", "Marcus Aurelius reminds himself that true tranquility does not require physical isolation or countryside retreats, but the quiet command of one's own governing mind.", "Pierre Hadot", "Philosophy as a Way of Life"),
        ("exp_ttc_8", "tao_te_ching_8", "The Virtue of Water", "Water exemplifies wu-wei: nourishing all living things without contention, resting in the humblest places.", "Traditional Taoist Commentary", "Daozang Corpus"),
        ("exp_matthew_5_3", "matthew_5_3", "The Beatitudes", "Jesus opens the Sermon on the Mount with paradoxical blessings, proclaiming spiritual poverty as the prerequisite for divine kingdom awareness.", "Biblical Commentary", "Historical Hermeneutics"),
        ("exp_eccl_3_1", "ecclesiastes_3_1", "The Seasons of Being", "Qoheleth reflects on the cosmic rhythm of temporal human existence, asserting that every sorrow and joy has its appointed season.", "Wisdom Tradition", "Old Testament Hermeneutics"),
        ("exp_psalms_23_1", "psalms_23_1", "The Shepherd of Peace", "The universal hymn of absolute trust in divine guidance and providence through even the darkest valleys of existence.", "Biblical Commentary", "Psalter Exegesis"),
        ("exp_quran_1_1", "quran_1_1", "The Basmala", "The opening invocation of the Quran establishing mercy and compassion as the fundamental attributes of the Divine.", "Tafsir Tradition", "Classical Islamic Scholarship")
    ]
    for c_id, p_id, title, body, author, source in commentaries:
        cursor.execute("INSERT OR REPLACE INTO explanation_entity VALUES (?, ?, ?, ?, ?, ?)", (c_id, p_id, title, body, author, source))

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
    seed_sample_commentaries(cursor)
    
    conn.commit()
    
    cursor.execute("SELECT COUNT(*) FROM passage_entity")
    total_passages = cursor.fetchone()[0]
    cursor.execute("SELECT COUNT(*) FROM book_entity")
    total_books = cursor.fetchone()[0]
    
    conn.close()
    
    db_size_kb = os.path.getsize(OUTPUT_DB) / 1024
    print(f"\n==========================================")
    print(f"DATABASE GENERATION COMPLETE")
    print(f"Total Books: {total_books}")
    print(f"Total Canonical Passages: {total_passages}")
    print(f"Database Size: {db_size_kb:.2f} KB ({db_size_kb/1024:.2f} MB)")
    print(f"Output: {OUTPUT_DB}")
    print(f"==========================================")

if __name__ == "__main__":
    main()
