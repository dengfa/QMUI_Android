from __future__ import annotations
import urllib.parse
import logging
import os
from urllib import request
import json
import ssl

# 用于显示进度条
from tqdm import tqdm
# 用于发起网络请求
import requests
# 用于多线程操作
import multitasking
import signal
# 导入 retry 库以方便进行下载出错重试
from retry import retry


signal.signal(signal.SIGINT, multitasking.killall)

# 请求头
headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36 QIHU 360SE'
}
# 定义 1 MB 多少为 B
MB = 1024**2


ssl._create_default_https_context = ssl._create_unverified_context

logging.basicConfig(level=logging.INFO)

urllogger = logging.getLogger("urllogger")
urllogger.addHandler(logging.FileHandler("url.log"))
urllogger.addHandler(logging.StreamHandler())

downloadlogger = logging.getLogger("downloadlogger")
downloadlogger.addHandler(logging.FileHandler("download.log"))
downloadlogger.addHandler(logging.StreamHandler())


def crawl(path, url):
    data = get_url(path, url)
    if data is None:
        pass
    elif is_file(data):
        download(path, data["files"])
    else:
        for u in data["files"]:
            sub_path = path + "/" + u["name"]
            crawl(sub_path, url)


def get_url(path, url):
    data = {
        "page_num": 1,
        "page_size": 30,   #没有处理分页的情况，写一个比较大的数
        "password": "",
        "path": path
    }
    try:
        req = request.Request(url)
        req.add_header('Content-Type', 'application/json; charset=utf-8')
        jsondata = json.dumps(data)
        jsondataasbytes = jsondata.encode('utf-8')  # needs to be bytes
        req.add_header('Content-Length', len(jsondataasbytes))
        response = urllib.request.urlopen(req, jsondataasbytes)
    except Exception as e:
        urllogger.error("[{}]链接抓取异常:{}".format(urllib.parse.unquote(url), e))
        return []
    res_json = json.loads(response.read())
    if res_json["code"] == 200:
        return res_json["data"]
    else:
        urllogger.info("[{}]获取数据状态码非200:{}".format(data, res_json))
        return None


def download(path, files):
    for i in files:
        url = i["url"]
        file_path = "." + path
        dirname = os.path.dirname(file_path)
        if os.path.exists(file_path):
            urllogger.info("[{}]文件已经存在，跳过".format(file_path))
            return
        elif os.path.exists(dirname):
            pass
        else:
            os.makedirs(dirname, exist_ok=True)
        try:
            do_download(url, file_path)
        except Exception as e:
            downloadlogger.error("下载文件[{}]失败[{}]".format(url, e))
            if os.path.exists(file_path):
                os.remove(file_path)
            #raise e


def is_file(data):
    if data["type"] == "file":
        return True
    else:
        return False


def split(start: int, end: int, step: int) -> list[tuple[int, int]]:
    # 分多块
    parts = [(start, min(start+step, end))
             for start in range(0, end, step)]
    return parts


def get_file_size(url: str, raise_error: bool = False) -> int:
    '''
    获取文件大小

    Parameters
    ----------
    url : 文件直链
    raise_error : 如果无法获取文件大小，是否引发错误

    Return
    ------
    文件大小（B为单位）
    如果不支持则会报错

    '''
    response = request.urlopen(url)
    file_size = response.headers.get('Content-Length')
    if file_size is None:
        if raise_error is True:
            raise ValueError('该文件不支持多线程分段下载！')
        return 0
    return int(file_size)


def do_download(url: str, file_name: str, retry_times: int = 3, each_size=16*MB) -> None:
    '''
    根据文件直链和文件名下载文件

    Parameters
    ----------
    url : 文件直链
    file_name : 文件名
    retry_times: 可选的，每次连接失败重试次数
    Return
    ------
    None

    '''
    f = open(file_name, 'wb')
    file_size = get_file_size(url)

    @retry(tries=retry_times)
    @multitasking.task
    def start_download(start: int, end: int) -> None:
        '''
        根据文件起止位置下载文件

        Parameters
        ----------
        start : 开始位置
        end : 结束位置
        '''
        _headers = headers.copy()
        # 分段下载的核心
        #_headers['Range'] = f'bytes={start}-{end}'
        # 发起请求并获取响应（流式）
        response = session.get(url, headers=_headers, stream=True)
        # 每次读取的流式响应大小
        chunk_size = 128
        # 暂存已获取的响应，后续循环写入
        # chunks = []
        f.seek(start)
        for chunk in response.iter_content(chunk_size=chunk_size):
            # 暂存获取的响应
            #chunks.append(chunk)
            f.write(chunk)
            # 更新进度条
            bar.update(chunk_size)
        
        # chunk in chunks:
            
        # 释放已写入的资源
       # del chunks

    session = requests.Session()
    # 分块文件如果比文件大，就取文件大小为分块大小
   
    #each_size = min(each_size, file_size)

    # 分块
    #parts = split(0, file_size, file_size)
   # print(f'分块数：{len(parts)}')
    # 创建进度条
    bar = tqdm(total=file_size, desc=f'下载文件：{file_name}')
    #for part in parts:
        #start, end = part
        #start_download(start, end)

    start_download(0, file_size)
    # 等待全部线程结束
    multitasking.wait_for_tasks()
    f.close()
    bar.close()


urls = [
    "",
    "/3喜马拉雅和B站/喜马拉雅/05.外语学习",
    "/7更新专区/2022课程更新/2022更新/004、英语课程/02.英语会员二区（名师）/16.奇文老师全集/奇文看美剧学口语（全10期）mp4",
    "/7更新专区/2022课程更新/2022更新/004、英语课程/02.英语会员二区（名师）/16.奇文老师全集/奇文英文杂志阅读（21节音频全）",
    "/7更新专区/2022课程更新/2022更新/004、英语课程/02.英语会员二区（名师）/08.李旭/2020版李旭纪录片",
    "/7更新专区/2022课程更新/2022更新/004、英语课程/02.英语会员二区（名师）/01.杨亮老师/3.杨亮《看懂原声电影的秘密》",
    "/7更新专区/2022课程更新/2022更新/004、英语课程/02.英语会员二区（名师）/15.善恩英语/023人类的故事",
]

failed = [
    "/7更新专区/2022课程更新/2022更新/004、英语课程/02.英语会员二区（名师）/11.杨家成/Jeremy杨家成霸道口语课等多个文件/Jeremy杨家成霸道口语课",
    "/7更新专区/2022课程更新/2022更新/004、英语课程/05.英语会员五区（更新中的课）/20.20全年经济 学人精读",
    "/7更新专区/2022课程更新/2022更新/004、英语课程/02.英语会员二区（名师）/17.恶魔奶爸",
]

success = [
    "/9-2 2023热门更新/外刊精读【赠送】/友邻优课/友邻优课（2015-2022）/17-20特色专辑/20专辑",
    "/7更新专区/2022课程更新/2022更新/004、英语课程/02.英语会员二区（名师）/15.善恩英语/007.【完结】善恩英语核心文法和写作/善恩英语-英语核心文法和写作",
    "/7更新专区/2022课程更新/2022更新/004、英语课程/02.英语会员二区（名师）/18.刘冠奇英语口语【3.0升级版】/3.口语教练【180节】",
    "/7更新专区/2022课程更新/2022更新/004、英语课程/02.英语会员二区（名师）/14.夏鹏-友邻优课",
    "/9-2 2023热门更新/外刊精读【赠送】/友邻优课/友邻优课（2015-2022）/友邻优课2022", #资料全面
    "/9-2 2023热门更新/外刊精读【赠送】/流利阅读", #资料全面
    "/7更新专区/2022课程更新/2022更新/004、英语课程/02.英语会员二区（名师）", #名师
    "/9-2 2023热门更新/友邻优课/热门平台2区等多个文件/友邻优课2023",
    "/9-2 2023热门更新/流利阅读/热门平台2区等多个文件/流利阅读2023",
    "/1课程资源/资料库合集/Y-英语会员/Y-英语会员/英语会员5区（更新中的课程）/34.2021 Shelly3月精读写作课（完结）",
]

# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    #递归下载path下的所有子目录
    #path = "/1课程资源/资料库合集/AA.主流平台会员/AA.主流平台会员/13、极客时间/01-专栏课/100-199/145-重学线性代数";
    path = urls[0]
    #url不用变
    url = "https://xingxingziyuan.haohanba.cn/api/public/path"
    crawl(path, url)


# See PyCharm help at https://www.jetbrains.com/help/pycharm/