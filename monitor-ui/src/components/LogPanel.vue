<template>
    <div>
        <el-collapse accordion>
            <el-collapse-item  class="log-item" v-for="log in data" :key="log.startTime">
                <div slot="title" style="width: 100%;height: 100%">
                    <el-row class="log-title-panel">
                        <el-col :span="2">   <el-tag class="process-item-pid">{{log.pid}}</el-tag></el-col>
                        <el-col :span="19" ><div class="log-title-name" ><span><b>{{log.sql}}</b></span></div></el-col>
                        <el-col :span="2" :offset="1">
                            <el-tag  type="info" style="width: 100px" >耗时 <b> {{log.costTime}} </b> ms </el-tag>
                        </el-col>
                    </el-row>
                </div>

                <div class="log-code">
                    <el-button style="float: right" type="warning" icon="el-icon-document-copy" @click="copyValue(log.sql)"  circle></el-button>
                    <highlightjs language="sql" :code="formatSql(log.sql)" />
                </div>
            </el-collapse-item>
        </el-collapse>
    </div>
</template>

<script>
import { format } from 'sql-formatter';

export default {
    name: "LogPanel",
    props:{
        data:Array
    },
    updated:function(){
        this.scrollToBottom();
    },
    methods:{
        formatSql(sql){
            return format(sql)
        },
        copyValue(sql){
            this.$copyText(this.formatSql(sql)).then(()=>{
                this.$notify({
                    title: '复制成功',
                    type: 'success'
                });
            }).catch((e)=>{
                this.$notify({
                    title: '复制失败',
                });
                console.error(e)
            })
        },
        scrollToBottom() {
            this.$nextTick(() => {
                let container = document.querySelector("html");
                container.scrollTop = container.scrollHeight;
            })
        }
    }
}
</script>

<style scoped>
.log-code{
    padding: 0px 20px;
}
.log-title-panel{
    width: 100%;
    height: 100%;
    padding: 0px 10px;
}
.log-title-name{
    width: 100%;
    border: 0px;
    margin: 0px;
    display: table;
}
.log-title-name > span{
    text-align: left;
    width: 100%;
    display: table-cell;
    overflow: hidden;
}

.log-title-name b{
    width: 10px;
    display: inline-block;
    white-space: nowrap;
}
</style>