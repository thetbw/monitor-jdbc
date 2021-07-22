<template>
    <div class="header">
        <span class="header-title" v-if="process">{{process.name}}
            <el-tag type="info" v-if="status == 1">连接中</el-tag>
            <el-tag type="success" v-if="status == 2">已连接</el-tag>
            <el-tag type="danger" v-if="status == 3">连接已关闭</el-tag>
        </span>
        <span class="header-action-group" v-if="process">
              <el-button style="float: right;margin-top: 5px" type="info" icon="el-icon-delete" @click="clearCallBack"  circle></el-button>
        </span>
    </div>
</template>

<script>
export default {
    name: "Header",
    props:{
        process:Object,
        clearCallBack:Function,
        status:Number
    },
    data(){
        return{

        }
    },
    created() {
    },
    methods:{

    }
}
</script>

<style scoped>
.header{
    width: 100%;
    height: 50px;
    background-color: cornflowerblue;
}
.header-title{
    display: inline-block;
    margin-left: 10px;
    vertical-align: middle;
    line-height: 50px;
    overflow: hidden;
    max-width: 900px;
    white-space: nowrap;
    text-overflow: ellipsis;
    font-size: 18px;
    font-weight: 800;
}
.header-action-group{
    float: right;
    margin-right: 20px;
}
</style>