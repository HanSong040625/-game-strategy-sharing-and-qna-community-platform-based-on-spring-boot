#!/usr/bin/env python3
import http.server
import socketserver
import os
import urllib.parse

class CustomHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def translate_path(self, path):
        # 解析路径
        path = urllib.parse.urlparse(path).path
        path = urllib.parse.unquote(path)
        
        # 处理/uploads/路径
        if path.startswith('/uploads/'):
            # 映射到项目根目录的uploads文件夹
            relative_path = path[len('/uploads/'):]
            return os.path.join(os.getcwd(), 'uploads', relative_path)
        
        # 处理/assets/avatars/路径
        if path.startswith('/assets/avatars/'):
            # 映射到frontend/src/assets/avatars文件夹
            relative_path = path[len('/assets/avatars/'):]
            return os.path.join(os.getcwd(), 'frontend', 'src', 'assets', 'avatars', relative_path)
        
        # 处理根路径和其他路径，映射到frontend/dist目录
        if path == '/' or not (path.startswith('/uploads/') or path.startswith('/assets/avatars/')):
            # 对于根路径和其他非/uploads、非/assets/avatars路径，都从frontend/dist目录提供
            if path == '/':
                path = '/index.html'
            
            # 确保路径不以/开头
            if path.startswith('/'):
                path = path[1:]
            
            return os.path.join(os.getcwd(), 'frontend', 'dist', path)
        
        # 默认情况
        return super().translate_path(path)
    
    def do_GET(self):
        try:
            # 调用父类的GET方法
            super().do_GET()
        except Exception as e:
            # 如果文件不存在，返回404
            self.send_error(404, "File not found")

def run_server(port=8081):
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    
    with socketserver.TCPServer(("", port), CustomHTTPRequestHandler) as httpd:
        print(f"Serving HTTP on 0.0.0.0 port {port} (http://localhost:{port}/)...")
        print("This server can serve:")
        print("- Frontend files from frontend/dist/")
        print("- Uploaded images from uploads/")
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\nShutting down server...")

if __name__ == "__main__":
    run_server()