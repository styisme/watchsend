#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import os
import sys
import subprocess
import configparser
import zipfile
import urllib.request
import shutil

ROOT = os.path.dirname(os.path.abspath(__file__))
INI_FILE = os.path.join(ROOT, "sign.ini")
LOCAL_GRADLE_DIR = os.path.join(ROOT, ".local_gradle")
GRADLE_VERSION = "5.6.4"

def setup_local_gradle():
    # 如果全局有 gradle，优先用全局的
    if shutil.which("gradle"):
        return "gradle"
    
    # 检查项目是否有 gradlew
    if os.path.exists(os.path.join(ROOT, "gradlew")):
        return "./gradlew"
        
    # 检查本地是否已经下载过
    local_bin = os.path.join(LOCAL_GRADLE_DIR, f"gradle-{GRADLE_VERSION}", "bin", "gradle")
    if os.path.exists(local_bin):
        return local_bin

    # 全都没有，自动下载（适配你锁定的 5.6.4 版本）
    print(f"\n📦 未检测到 Gradle，正在自动下载 Gradle {GRADLE_VERSION}...")
    os.makedirs(LOCAL_GRADLE_DIR, exist_ok=True)
    zip_path = os.path.join(LOCAL_GRADLE_DIR, "gradle.zip")
    url = f"https://services.gradle.org/distributions/gradle-{GRADLE_VERSION}-bin.zip"
    
    try:
        urllib.request.urlretrieve(url, zip_path)
        print("📦 下载完成，正在解压...")
        with zipfile.ZipFile(zip_path, 'r') as zip_ref:
            zip_ref.extractall(LOCAL_GRADLE_DIR)
        os.remove(zip_path)
        
        # 赋予执行权限 (Android 下部分文件系统需要)
        os.chmod(local_bin, 0o755)
        print("✅ Gradle 本地环境准备就绪！")
        return local_bin
    except Exception as e:
        print(f"❌ 自动下载 Gradle 失败: {e}")
        print("请检查网络，或手动在电脑/Termux下载后放入项目根目录的 .local_gradle 文件夹。")
        sys.exit(1)

def load_sign_config():
    if not os.path.exists(INI_FILE):
        return None
    config = configparser.ConfigParser()
    config.read(INI_FILE, encoding="utf-8")
    if "signing" in config:
        return {
            "keystore": config.get("signing", "keystore", fallback=""),
            "store_password": config.get("signing", "store_password", fallback=""),
            "key_alias": config.get("signing", "key_alias", fallback=""),
            "key_password": config.get("signing", "key_password", fallback="")
        }
    return None

def prompt_sign_config():
    print("\n⚠️ 未找到 sign.ini 或配置不完整，请手动输入签名信息：")
    keystore = input("1. 密钥文件绝对路径 (如 /sdcard/release.keystore): ").strip()
    store_password = input("2. 密钥库密码 (Store Password): ").strip()
    key_alias = input("3. 密钥别名 (Key Alias): ").strip()
    key_password = input("4. 密钥密码 (Key Password): ").strip()
    
    if not keystore or not store_password or not key_alias or not key_password:
        print("❌ 信息不完整，取消编译。")
        sys.exit(1)
        
    config = configparser.ConfigParser()
    config["signing"] = {
        "keystore": keystore,
        "store_password": store_password,
        "key_alias": key_alias,
        "key_password": key_password
    }
    
    with open(INI_FILE, "w", encoding="utf-8") as f:
        config.write(f)
    print(f"✅ 配置已自动保存至 {INI_FILE} (请勿上传此文件至 Git)")
    
    return {
        "keystore": keystore,
        "store_password": store_password,
        "key_alias": key_alias,
        "key_password": key_password
    }

def run_gradle(task):
    gradle_cmd = setup_local_gradle()
    print(f"\n🚀 开始执行 {gradle_cmd} {task} ...")
    
    env = os.environ.copy()
    # 尝试找到 JAVA_HOME，如果是 Pydroid3，通常自带 java
    if "JAVA_HOME" not in env and os.path.exists("/data/data/ru.iiec.pydroid3/files/usr/opt/java"):
         env["JAVA_HOME"] = "/data/data/ru.iiec.pydroid3/files/usr/opt/java"
    
    try:
        process = subprocess.Popen(
            [gradle_cmd, task, "--no-daemon"],
            cwd=ROOT,
            env=env,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            universal_newlines=True,
            bufsize=1
        )
        
        for line in process.stdout:
            print(line, end="")
            
        process.wait()
        if process.returncode == 0:
            print(f"\n🎉 {task} 编译成功！")
            apk_path = os.path.join(ROOT, "app/build/outputs/apk", task.replace("assemble", "").lower())
            print(f"📦 APK 路径: {apk_path}")
        else:
            print(f"\n💥 编译失败，请检查日志。")
            
    except Exception as e:
        print(f"❌ 运行出错: {e}")

def main():
    print("=" * 40)
    print("      WatchSend 本地编译工具")
    print(f"      当前路径: {ROOT}")
    print("=" * 40)
    print("1. Debug 编译 (无需签名)")
    print("2. Release 编译 (需要签名)")
    print("0. 退出")
    print("=" * 40)
    
    choice = input("请选择编译模式 [0-2]: ").strip()
    
    if choice == "0":
        sys.exit(0)
    elif choice == "1":
        run_gradle("assembleDebug")
    elif choice == "2":
        sign_config = load_sign_config()
        if not sign_config or not all(sign_config.values()):
            sign_config = prompt_sign_config()
            
        os.environ["NAS_KEYSTORE_PATH"] = sign_config["keystore"]
        os.environ["NAS_KEYSTORE_PASSWORD"] = sign_config["store_password"]
        os.environ["NAS_KEY_ALIAS"] = sign_config["key_alias"]
        os.environ["NAS_KEY_PASSWORD"] = sign_config["key_password"]
        
        run_gradle("assembleRelease")
    else:
        print("无效选项！")

if __name__ == "__main__":
    main()
