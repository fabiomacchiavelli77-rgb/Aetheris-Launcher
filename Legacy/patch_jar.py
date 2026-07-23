import zipfile
import json
import os

def patch_fabric_mod_json(json_bytes):
    try:
        data = json.loads(json_bytes.decode('utf-8'))
        if "depends" in data:
            if "fabricloader" in data["depends"]:
                data["depends"]["fabricloader"] = "*"
            if "minecraft" in data["depends"]:
                data["depends"]["minecraft"] = "*"
        
        if data.get("id") == "cloth-config":
            if "accessWidener" in data:
                del data["accessWidener"]
            if "classTweaker" in data:
                del data["classTweaker"]
                
        return json.dumps(data).encode('utf-8')
    except Exception as e:
        print("Errore nel patching del json:", e)
        return json_bytes

def process_jar(jar_path):
    print(f"Processing {jar_path}")
    temp_jar = jar_path + ".tmp"
    with zipfile.ZipFile(jar_path, 'r') as zin:
        with zipfile.ZipFile(temp_jar, 'w', zipfile.ZIP_DEFLATED) as zout:
            for item in zin.infolist():
                data = zin.read(item.filename)
                if item.filename == "fabric.mod.json":
                    data = patch_fabric_mod_json(data)
                elif item.filename.endswith(".jar"):
                    nested_jar_path = jar_path + "_nested"
                    with open(nested_jar_path, "wb") as f:
                        f.write(data)
                    process_jar(nested_jar_path)
                    with open(nested_jar_path, "rb") as f:
                        data = f.read()
                    os.remove(nested_jar_path)
                zout.writestr(item.filename, data)
    os.remove(jar_path)
    os.rename(temp_jar, jar_path)

if __name__ == "__main__":
    process_jar("src/main/resources/aristois-seed-cracker.jar")
    print("All jars patched successfully!")
