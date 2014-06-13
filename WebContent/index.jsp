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
   <table class="table table-striped table-bordered" style="width:900px;margin:0 auto;margin-top:20px;">
        <tr><td colspan="3"><h3>移动设备管理（IOS Mobile Device Management）</h3></td></tr>
		<tr>
			<td style="vertical-align: middle;"><label class="sr-only" for="mdmEmail">邮箱地址：</label><input type="email" class="form-control" id="mdmEmail"/></td>
			<td style="vertical-align: middle;text-align: center;"><button type="button" id="codeButton" onclick="getCode()" class="btn btn-default">获取代码</button></td>
			<td><input type="text" class="form-control" style="width: 300px;" id="mdmCode"/></td>
		</tr>
		<tr>
		  <td style="vertical-align: top;width: 350px;">
		   <div class="alert alert-warning"><span class="label label-warning">使用注意:</span><br/>
			   <p>
			   	第一步：点击'获取代码'按钮, 通过URL安装CA、mobileconfig文件到你的设备上。
			   </p>
			   <p>
			   	第二步：使用下面的命令控制设备。
			   </p>
		   </div>
		   <table style="width: 350px;">
                 <tr>
                  <td align="center">
                  <a id="lock" class="btn btn-default" href="javascript:execCommand('lock');">锁屏</a>&nbsp;&nbsp;
                  <a id="clear" class="btn btn-default" href="javascript:execCommand('clear');">清除密码</a>&nbsp;&nbsp;
                  <a id="erase" class="btn btn-default" href="javascript:execCommand('erase');">清除数据</a>
                 </td>
                 </tr>
           </table>
		  </td>
          <td colspan="2" style="vertical-align: middle; text-align: center;">
            <table id="downLoad" style="width: 500px;text-align: left;">
            <tr>
               <td style="border-left: 1px solid #F9F9F9;vertical-align: middle;">
                CA证书下载安装: <br/>
                <input type="text" readonly="readonly" class="form-control" style="width: 415px;" id="CAconfig"/>
               </td>
              </tr>
              <tr>
               <td style="border-left: 1px solid #F9F9F9;vertical-align: middle;">
                                            描述文件mobileconfig下载安装: <br/>
                <input type="text" readonly="readonly" class="form-control" style="width: 415px;" id="mdmMobileconfig"/>
                
               </td>
              </tr>
            </table>
          </td>
        </tr>
        <tr>
          <td colspan="3">
           <div id="infoMsg" class="alert alert-warning" style="width: 820px;">无</div>           
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
		    alert("请输入邮箱地址!");
		    $("#mdmEmail").focus(); 
		    return false; 
	   } 
	   if(!$("#mdmEmail").val().match(/^\w+((-\w+)|(\.\w+))*\@[A-Za-z0-9]+((\.|-)[A-Za-z0-9]+)*\.[A-Za-z0-9]+$/)){ 
		    alert("请输入合法的邮箱地址!");
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
	              $("#CAconfig").val("https://192.168.1.150:8443/MDMServer/mdmtool/down/ca.crt");
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
  </script> 
  </body>
</html>