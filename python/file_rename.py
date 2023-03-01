import urllib.parse
import logging
import os
from urllib import request
import json

logging.basicConfig(level=logging.INFO)

urllogger = logging.getLogger("urllogger")
urllogger.addHandler(logging.FileHandler("url.log"))
urllogger.addHandler(logging.StreamHandler())

if __name__ == '__main__':
    targetpath = ""
    g = os.walk(targetpath)
    for path,dir_list,file_list in g:
        for file_name in file_list:
            folder = os.path.basename(path).strip()
            suffix = os.path.splitext(file_name)[-1].strip()
            file_name_without_suffix = os.path.splitext(file_name)[0].strip()
            if suffix == ".mp3":
                urllogger.info("path:{}\nfolder:{}\nsuffix:{}".format(path, folder,suffix))
                newName = folder[:4] + "_" + file_name_without_suffix + "_" + folder[4:].strip() + suffix
                urllogger.info("newName:{}\n".format(newName))
                os.replace(os.path.join(path,file_name),os.path.join(path,newName))