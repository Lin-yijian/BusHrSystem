const fs = require('fs')
const path = require('path')



// 获取domainkey
const pkJsonPath = path.resolve(__dirname, './package.json')
const pkJsonStr = fs.readFileSync(pkJsonPath).toString()
const pkJson = JSON.parse(pkJsonStr)
const domainKey = pkJson.domainKey
// static目录，获取verison信息
const staticPath = path.resolve(__dirname, `./static`)
const versionPath = staticPath + `/public/${domainKey}/version.json`
let versionStr = fs.readFileSync(versionPath).toString()
const versionObj = JSON.parse(versionStr)
const version = versionObj.version
// 创建打包目录
const buildproductPath = path.resolve(__dirname, './yonyoucloud-buildproduct')
function deleteDir(url){
    var files = [];

    if( fs.existsSync(url) ) {  //判断给定的路径是否存在

        files = fs.readdirSync(url);   //返回文件和子目录的数组
        files.forEach(function(file,index){
            var curPath = path.join(url,file);

            if(fs.statSync(curPath).isDirectory()) { //同步读取文件夹文件，如果是文件夹，则函数回调
                deleteDir(curPath);
            } else {
                fs.unlinkSync(curPath);    //是指定文件，则删除
            }

        });

        fs.rmdirSync(url); //清除文件夹
    }else{
        console.log("给定的路径不存在！");
    }

}

deleteDir(buildproductPath)
fs.mkdirSync(buildproductPath)
fs.mkdirSync(buildproductPath + '/' + domainKey)
fs.mkdirSync(buildproductPath + '/' + domainKey + '/version')
fs.mkdirSync(buildproductPath + '/' + domainKey + '/' + version)
fs.mkdirSync(buildproductPath + '/' + domainKey + '/' + version + '/config')



function copyFolderSync(source, target) {
    // 如果目标文件夹不存在，则创建
    if (!fs.existsSync(target)) {
        fs.mkdirSync(target)
    }

    // 获取源文件夹下的所有文件和子文件夹
    const files = fs.readdirSync(source)

    // 遍历文件和子文件夹，并递归拷贝
    for (const file of files) {
        const sourcePath = path.join(source, file)
        const targetPath = path.join(target, file)

        if (fs.lstatSync(sourcePath).isDirectory()) {
            copyFolderSync(sourcePath, targetPath)
        } else {
            fs.copyFileSync(sourcePath, targetPath)
        }
    }
    return true

}

copyFolderSync(staticPath + `/public/${domainKey}`, buildproductPath + '/' + domainKey + '/' + version)
//copyFolderSync(path.resolve(__dirname, './conf-build'), buildproductPath + '/' + domainKey + '/' + version + '/config')
//copyFolderSync(path.resolve(__dirname, './conf-build'), buildproductPath + '/' + domainKey + '/' + version)


fs.copyFileSync(path.resolve(__dirname, './module.xml'), buildproductPath + '/' + domainKey + '/module.xml')
fs.copyFileSync(staticPath + `/public/${domainKey}/version.json`, buildproductPath + '/' + domainKey + '/version/private_version.json')