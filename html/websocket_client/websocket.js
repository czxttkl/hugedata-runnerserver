// JavaScript Document

var webSocket;

function connect()
{
    try
    {
        var readyState=new Array("正在连接","已建立连接","正在关闭连接","已关闭连接");
        var host="ws://localhost:9999";
        webSocket=new WebSocket(host);
        var message=document.getElementById("message");
        message.innerHTML+="<p>Socket状态："+readyState[webSocket.readyState]+"</p>";
        webSocket.onopen=function()
        {
            message.innerHTML+="<p>Socket状态："+readyState[webSocket.readyState]+"</p>";
        }
        webSocket.onmessage=function(msg)
        {
            message.innerHTML+="<p>接收信息："+msg.data+"</p>";
			generateNewPage(msg.data);
        }
        webSocket.onclose=function()
        {
            message.innerHTML+="<p>Socket状态："+readyState[webSocket.readyState]+"</p>";
        }
    }
    catch(exception)
    {
    message.innerHTML+="<p>有错误发生</p>";
    }
}

function generateNewPage(data){
	
	if(data!="StartTest" && data!="Add To Queue Successful"){
	 var locPrefix = "file:///p:/java/hugedata-runnerserver/";
	 var locSuffix = "/html/index.html"
	 var resultdir = data.split(":")[1];
	// alert(locPrefix + resultdir + locSuffix);
	 window.open(locPrefix + resultdir + locSuffix);
	}
}

function send()
{
    var text=document.getElementById("text").value;
    var message=document.getElementById("message");
    if(text=="")
    {
    message.innerHTML+="<p>请输入一些文字</p>";
        return;
    }
    try
    {
        webSocket.send(text);
        message.innerHTML+="<p>发送数据："+text+"</p>";
    }
    catch(exception)
    {
    message.innerHTML+="<p>发送数据出错</p>";
    }
    document.getElementById("text").value="";
}

function disconnect()
{
    webSocket.close();
}
