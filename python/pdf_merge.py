import os
from PyPDF2 import PdfMerger

target_path = '/Users/dengfa/Desktop/english/2023外刊精读【精品】/友邻优课/友邻优课2023/test'
file_merger = PdfMerger()
#file_merger.append(os.path.join(path,file_name))     # 合并pdf文件
#file_merger.write(os.path.join(target_path,"merged.pdf"))


g = os.walk(target_path)
for path,dir_list,file_list in g:
    for file_name in file_list:
        folder = os.path.basename(path).strip()
        suffix = os.path.splitext(file_name)[-1].strip()
        file_name_without_suffix = os.path.splitext(file_name)[0].strip()
        if suffix == ".pdf":
            newName = folder[:4] + "." + file_name
            print("newName:{}\n".format(newName))
            os.replace(os.path.join(path,file_name),os.path.join(path,newName))

