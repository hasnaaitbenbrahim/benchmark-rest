#!/usr/bin/env python3
"""
Script pour générer les fichiers CSV nécessaires pour les tests JMeter
"""
import os
import json

JMETER_DIR = "jmeter/data"
os.makedirs(JMETER_DIR, exist_ok=True)

print("📝 Génération des fichiers CSV pour JMeter...")

# 1. Générer ids.csv avec des IDs valides
print("Génération de ids.csv...")
with open(f"{JMETER_DIR}/ids.csv", "w") as f:
    f.write("itemId,categoryId\n")
    # Générer 1000 paires itemId/categoryId
    # Items: 1-100000, Categories: 1-2000
    for i in range(1, 1001):
        item_id = 1 + (i % 100000)
        category_id = 1 + (i % 2000)
        f.write(f"{item_id},{category_id}\n")

print(f"✅ ids.csv généré avec {sum(1 for _ in open(f'{JMETER_DIR}/ids.csv'))} lignes")

# 2. Générer payloads_1k.csv (payloads légers ~1KB)
print("Génération de payloads_1k.csv...")
with open(f"{JMETER_DIR}/payloads_1k.csv", "w") as f:
    f.write("payload\n")
    # Générer 50 payloads de ~1KB chacun
    for i in range(1, 51):
        sku = f"SKU{i:06d}"
        name = f"Item Test {i}"
        price = round(10.0 + (i % 90), 2)
        stock = i % 500
        category_id = 1 + (i % 2000)
        
        # Créer un payload JSON avec padding pour atteindre ~1KB
        base_payload = {
            "sku": sku,
            "name": name,
            "price": price,
            "stock": stock,
            "category": {"id": category_id},
            "description": ""
        }
        base_json = json.dumps(base_payload, separators=(',', ':'))
        target_size = 1000
        pad_length = max(0, target_size - len(base_json) - 20)  # -20 pour marge
        pad = "x" * pad_length
        base_payload["description"] = pad
        payload = json.dumps(base_payload, separators=(',', ':'))
        f.write(f"{payload}\n")

print(f"✅ payloads_1k.csv généré avec {sum(1 for _ in open(f'{JMETER_DIR}/payloads_1k.csv'))} lignes")

# 3. Générer payloads_5k.csv (payloads lourds ~5KB)
print("Génération de payloads_5k.csv...")
with open(f"{JMETER_DIR}/payloads_5k.csv", "w") as f:
    f.write("payload\n")
    # Générer 20 payloads de ~5KB chacun
    for i in range(1, 21):
        sku = f"SKU{i:06d}"
        name = f"Item Heavy Test {i}"
        price = round(10.0 + (i % 90), 2)
        stock = i % 500
        category_id = 1 + (i % 2000)
        
        # Créer un payload JSON avec padding pour atteindre ~5KB
        base_payload = {
            "sku": sku,
            "name": name,
            "price": price,
            "stock": stock,
            "category": {"id": category_id},
            "description": ""
        }
        base_json = json.dumps(base_payload, separators=(',', ':'))
        target_size = 5000
        pad_length = max(0, target_size - len(base_json) - 20)  # -20 pour marge
        pad = "y" * pad_length
        base_payload["description"] = pad
        payload = json.dumps(base_payload, separators=(',', ':'))
        f.write(f"{payload}\n")

print(f"✅ payloads_5k.csv généré avec {sum(1 for _ in open(f'{JMETER_DIR}/payloads_5k.csv'))} lignes")

print("\n📊 Résumé des fichiers générés :")
print(f"  - ids.csv: {sum(1 for _ in open(f'{JMETER_DIR}/ids.csv'))} lignes")
print(f"  - payloads_1k.csv: {sum(1 for _ in open(f'{JMETER_DIR}/payloads_1k.csv'))} lignes")
print(f"  - payloads_5k.csv: {sum(1 for _ in open(f'{JMETER_DIR}/payloads_5k.csv'))} lignes")
print(f"\n✅ Tous les fichiers CSV ont été générés dans {JMETER_DIR}/")

