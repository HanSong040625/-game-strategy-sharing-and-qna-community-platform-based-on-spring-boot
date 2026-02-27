#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
文件上传路径测试脚本
用于定位文件上传路径配置问题
"""

import os
import sys

def check_paths():
    """检查所有相关路径"""
    print("=== 文件上传路径测试 ===\n")
    
    # 1. 检查当前工作目录
    cwd = os.getcwd()
    print(f"1. 当前工作目录: {cwd}")
    print(f"   存在: {os.path.exists(cwd)}")
    
    # 2. 检查期望的上传目录
    expected_upload_dir = os.path.join(cwd, "frontend", "src", "assets", "resources")
    print(f"\n2. 期望的上传目录: {expected_upload_dir}")
    print(f"   存在: {os.path.exists(expected_upload_dir)}")
    if os.path.exists(expected_upload_dir):
        print(f"   可写: {os.access(expected_upload_dir, os.W_OK)}")
    
    # 3. 检查错误信息中的路径
    error_path = r"C:\Users\86199\AppData\Local\Temp\tomcat.8080.895363830936813452\work\Tomcat\localhost\ROOT\frontend\src\assets\resources"
    print(f"\n3. 错误信息中的路径: {error_path}")
    print(f"   存在: {os.path.exists(error_path)}")
    
    # 4. 检查项目根目录
    project_root = cwd
    print(f"\n4. 项目根目录: {project_root}")
    print(f"   存在: {os.path.exists(project_root)}")
    
    # 5. 检查frontend目录
    frontend_dir = os.path.join(project_root, "frontend")
    print(f"\n5. frontend目录: {frontend_dir}")
    print(f"   存在: {os.path.exists(frontend_dir)}")
    
    # 6. 检查src目录
    src_dir = os.path.join(frontend_dir, "src")
    print(f"\n6. src目录: {src_dir}")
    print(f"   存在: {os.path.exists(src_dir)}")
    
    # 7. 检查assets目录
    assets_dir = os.path.join(src_dir, "assets")
    print(f"\n7. assets目录: {assets_dir}")
    print(f"   存在: {os.path.exists(assets_dir)}")
    
    # 8. 检查resources目录
    resources_dir = os.path.join(assets_dir, "resources")
    print(f"\n8. resources目录: {resources_dir}")
    print(f"   存在: {os.path.exists(resources_dir)}")
    if os.path.exists(resources_dir):
        print(f"   可写: {os.access(resources_dir, os.W_OK)}")
    
    # 9. 检查uploads目录（原始配置）
    uploads_dir = os.path.join(project_root, "uploads")
    print(f"\n9. uploads目录（原始配置）: {uploads_dir}")
    print(f"   存在: {os.path.exists(uploads_dir)}")
    
    # 10. 检查临时目录
    temp_dir = r"C:\Users\86199\AppData\Local\Temp"
    print(f"\n10. 系统临时目录: {temp_dir}")
    print(f"    存在: {os.path.exists(temp_dir)}")
    print(f"    可写: {os.access(temp_dir, os.W_OK)}")

def check_java_resources():
    """检查Java资源路径配置"""
    print("\n=== Java资源路径检查 ===\n")
    
    # 检查target/classes目录
    target_classes = os.path.join(os.getcwd(), "target", "classes")
    print(f"1. target/classes目录: {target_classes}")
    print(f"   存在: {os.path.exists(target_classes)}")
    
    # 检查application.yml文件
    app_yml = os.path.join(target_classes, "application.yml")
    print(f"\n2. application.yml文件: {app_yml}")
    print(f"   存在: {os.path.exists(app_yml)}")
    
    if os.path.exists(app_yml):
        try:
            with open(app_yml, 'r', encoding='utf-8') as f:
                content = f.read()
                print("   内容预览:")
                for line in content.split('\n')[:10]:
                    if line.strip() and not line.strip().startswith('#'):
                        print(f"     {line}")
        except Exception as e:
            print(f"   读取失败: {e}")

def main():
    """主函数"""
    print("开始文件上传路径测试...\n")
    
    check_paths()
    check_java_resources()
    
    print("\n=== 测试完成 ===")
    print("请检查上述路径配置，特别是resources目录是否存在且可写")

if __name__ == "__main__":
    main()