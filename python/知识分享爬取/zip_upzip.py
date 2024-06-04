import os
import zipfile

dir_path = "/Users/dengfa/Desktop/english/附录1：全书词伙分类列表"  # 替换为目标目录路径

# 遍历目标目录下的所有文件和文件夹
for root, dirs, files in os.walk(dir_path):
    for file in files:
        if file.endswith(".zip"):  # 只处理zip文件
            file_path = os.path.join(root, file)
            with zipfile.ZipFile(file_path, 'r') as zip_ref:
                zip_ref.extractall(root)  # 解压缩到同级目录