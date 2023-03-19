我是码斯(MaSi)。

利用[CodeGeeX](https://github.com/THUDM/CodeGeeX)的代码生成能力，动嘴直接生成代码。

本项目的部分代码借鉴了CodeGeeX等工具生成的结果，但由于它们是模型的输出，因此无法得知具体出处，无法在代码中标注。如您知晓，请赐教。


* 程序运行说明(假设项目根目录为 /path/to/masi )

（1）在安装有JDK 1.8+的机器上：cd /path/to/masi && mvn clean install

（2）rm -rf deploy && mkdir -p deploy/bin && mkdir -p deploy/conf && cp target/masi-1.0-SNAPSHOT-jar-with-dependencies.jar src/main/scripts/*.sh deploy/bin && cp src/main/resources/*.properties deploy/conf

（3）cd deploy/bin; vim start-masi.sh 填写脚本中用到的各种API Key及API Secret

（4）./start-masi.sh
