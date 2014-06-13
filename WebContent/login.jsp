<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="common/taglibs.jsp"%>
<!DOCTYPE html>
<html lang="zh-cn">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>IOS Mobile Device Management</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" /> 
    <link href="${bootstrap_ctx }/css/bootstrap.min.css" rel="stylesheet" />
    <link href="${bootstrap_ctx }/css/bootstrap-responsive.min.css" rel="stylesheet" />
    <script type="text/javascript" src="${js_ctx }/jquery.js"></script>
    <style type="text/css">
       #loading{
         display:none; 
         position:fixed;
         _position:absolute;
         top:50%;
         left:50%;
         width:124px;
         height:124px;
         overflow:hidden;
         background:url(${images_ctx}/loaderc.gif) no-repeat;
         z-index:7; 
         margin:-62px 0 0 -62px;
       } 
    </style>
</head>
  <body>
  <form class="form-inline">
   <table class="table table-striped table-bordered" style="width:50%;margin:0 auto;margin-top:20px;">
        <tr><td><h3>移动设备管理（IOS Mobile Device Management）</h3></td></tr>
        <tr><td><h5>第一步：填写邮箱地址</h5></td></tr>
		<tr>
			<td style="vertical-align: middle;"><label class="sr-only" for="mdmEmail">邮箱地址：</label><input type="email" class="form-control" id="mdmEmail"/></td>
		</tr>
		<tr><td><h5>第二步：获取代码</h5></td></tr>
		<tr>
			<td style="vertical-align: middle;"><button type="button" id="codeButton" onclick="getCode()" class="btn btn-default">获取代码</button>
			<input type="text" class="form-control" style="width: 300px;" id="mdmCode"/></td>
		</tr>
        <tr><td><h5>第三步：下载证书文件</h5></td></tr>
        <tr>
        	<td style="vertical-align: middle;">
        		<button type="button" id="codeButton" onclick="downCA()" class="btn btn-default">安装CA证书</button>
        		<button type="button" id="codeButton" onclick="downConfig()" class="btn btn-default">安装描述文件</button>
        	</td>
        </tr>
        <tr><td><h5>第四步：设备操作</h5></td></tr>
        <tr>
        	<td style="vertical-align: middle;">
        		<a id="lock" class="btn btn-default" href="javascript:execCommand('lock');">锁屏</a>&nbsp;&nbsp;
            	<a id="clear" class="btn btn-default" href="javascript:execCommand('clear');">清除密码</a>&nbsp;&nbsp;
            	<a id="erase" class="btn btn-default" href="javascript:execCommand('erase');">清除数据</a>
        	</td>
        </tr>
        <tr><td><h5>第五步：操作结果</h5></td></tr>
        <tr>
        	<td>
        		<div id="infoMsg" class="alert alert-warning">无</div>         
        	</td>
        </tr>
   </table>
  </form>
  <div id="loading"></div> 
  <script type="text/javascript">
  
    $(document).ready(function(){
	   $("#lock").attr("readonly","readonly");
       $("#clear").attr("readonly","readonly");
       $("#erase").attr("readonly","readonly");
       $("#mdmCode").attr("readonly","readonly");
	});
	
	function execCommand(command){
	   $("#infoMsg").html("");
	   jQuery("#loading").fadeIn();
       var dcode = $("#mdmCode").val();
       var httpcurl = "${ctx}/mdm/" + command + "/" + dcode + ".do";
       $.ajax({ 
           type: "post", 
           url: httpcurl, 
           dataType: "json", 
           success: function (data) { 
              jQuery("#loading").fadeOut();  
              $("#infoMsg").html(data.msg);
           }, 
           error: function (XMLHttpRequest, textStatus, errorThrown) { 
              jQuery("#loading").fadeOut();  
           } 
       });
    }
    
	function checkEmail(){ 
       if($("#mdmEmail").val()==""){ 
		    alert("Your email address cannot be empty!");
		    $("#mdmEmail").focus(); 
		    return false; 
	   } 
	   if(!$("#mdmEmail").val().match(/^\w+((-\w+)|(\.\w+))*\@[A-Za-z0-9]+((\.|-)[A-Za-z0-9]+)*\.[A-Za-z0-9]+$/)){ 
		    alert("The email address format error!");
		    $("#mdmEmail").focus(); 
		    return false; 
	   } 
	   return true; 
    } 

    function getCode(){
       if(checkEmail()){
           $("#codeButton").attr("disabled","disabled");
	       jQuery("#loading").fadeIn();
	       $("#mdmEmail").attr("readonly","readonly");
	       var email = $("#mdmEmail").val();
	       $.ajax({ 
	           type: "post", 
	           url: "${ctx}/mdm/getCode.do?email="+email, 
	           dataType: "json", 
	           success: function (data) { 
	              $("#mdmCode").val(data.code); 
	              $("#mdmMobileconfig").val(data.mobileconfig); 
	              $("#mdmCode").attr("readonly","readonly");
	              
	              $("#lock").attr("disabled",false);
	              $("#clear").attr("disabled",false);
	              $("#erase").attr("disabled",false);
	              $("#qrcode").attr("src","${ctx}/"+data.qrimg);
	              jQuery("#loading").fadeOut();
	           },
	           error:function(){
	              jQuery("#loading").fadeOut();  
	              alert("I'm sorry something wrong!");
	           }
	       });
       }
    }
    
    function downCA(){
    	
    }
  </script> 
  </body>
</html>