<template>
    <div id="app">
        <el-container style="height: 100%">
            <el-header style="padding: 0px;height:60px;width:100%;position: fixed;top:0px;z-index:10"><Header :process="currentProcess"  :status="wsStatus" :clear-call-back="clearLog"></Header></el-header>
            <el-main style="margin-top: 60px"><log-panel :data="logs"></log-panel></el-main>
        </el-container>
        <el-dialog title="选择一个进程"  :visible.sync="processSelectShow" :show-close="false" :closeOnClickModal="false" :close-on-press-escape="false">
            <span slot="title">
                <span class="el-dialog__title">选择一个进程</span>
                <el-button @click="showProcess" type="primary" icon="el-icon-refresh-left" style="float: right" circle></el-button>
            </span>
            <div>
                <el-row type="type" align="middle" class="process-row" v-for="item in process" :key="item.pid">
                    <div  class="process-item">
                        <el-tag class="process-item-pid">{{item.pid}}</el-tag>
                        <span class="process-item-name"><b>{{item.name}}</b></span>
                        <el-button type="text" style="float: right" @click="attachProcess(item.pid)">连接</el-button>
                    </div>
                </el-row>
            </div>

        </el-dialog>
    </div>
</template>

<script>
import Header from './components/Header.vue'
import LogPanel from "./components/LogPanel.vue";
import service from "@/utils/request";


export default {
    name: 'App',
    components: {
        Header,LogPanel
    },
    data(){
        return{
            logs:[],
            processSelectShow:false,
            process:[],
            currentProcess:null,
            wsStatus:0 //连接状态 0 还未连接 1链接中 2 连接完成 3 已关闭
        }
    },
    created() {
        this.showProcess()
    },
    methods:{
        //显示java所有进程
        showProcess(){
            this.process = []
            service({
                url:"/api/process",
                method:"GET"
            }).then(data=>{
                this.processSelectShow = true
                this.process = data
            })
        },
        //附加到目标进程
        attachProcess(pid){
            service({
                url:"/api/process/attach",
                method:"GET",
                params:{pid:pid}
            }).then(data=>{
                this.currentProcess = data
                this.processSelectShow = false
                this.openWebSocket(pid)
            })
            console.log(pid)
        },
        openWebSocket(pid){
            console.log(pid)
            this.wsStatus = 1
            this.ws = new WebSocket("ws://127.0.0.1:10086/api/log/reader/"+pid)
            this.ws.onopen = () =>{
                this.wsStatus = 2
                console.log("websocket连接成功");
                this.ws.send("客户端连接成功");
            }
            this.ws.onmessage = (e) => {
                let data = e.data;
                this.logs.push(JSON.parse(data))
            }
            this.ws.onclose =  () =>{
                this.wsStatus = 3
                console.log("webscoket 连接已关闭")
            }
            this.ws.onerror = (e)=>{
                this.wsStatus = 3
                console.error(e)
                console.error("websocket连接异常")
            }
        },

        stopProcess(){

        },
        clearLog(){
            this.logs = []
        }
    }
}
</script>

<style>

body{
    margin: 0px;
    padding: 0px;
}
.process-item-pid{
    width: 70px;
    text-align: center;
    display: inline-block;
}

.process-item-name{
    display: inline-block;
    margin-left: 10px;
    vertical-align: middle;
    line-height: 50px;
    overflow: hidden;
    max-width: 500px;
    white-space: nowrap;
    text-overflow: ellipsis;
}
.process-item{
    box-shadow: 0 2px 4px rgba(0, 0, 0, .12), 0 0 6px rgba(0, 0, 0, .04);
    vertical-align: center;
    height: 50px;
    padding: 0px 10px;
}
.process-row{
    margin-bottom: 20px;
}
</style>
