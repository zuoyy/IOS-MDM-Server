package com.zuoyy.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.zuoyy.common.qrcode.TwoDimensionCode;
import com.zuoyy.common.utils.ConfigUtils;
import com.zuoyy.common.utils.DateUtils;
import com.zuoyy.common.utils.MdmUtils;
import com.zuoyy.common.utils.PushUtils;
import com.zuoyy.common.utils.StringUtils;
import com.zuoyy.pojo.Command;
import com.zuoyy.pojo.Mdm;
import com.zuoyy.service.CommandService;
import com.zuoyy.service.MdmService;

@Controller
@RequestMapping("/mdm")
public class MdmController {
	
	private MdmService mdmService;
	private CommandService commandService;
	
	/**
     * 获取设备Code
     * @throws Exception
     */
	@ResponseBody
    @RequestMapping("/getCode")
    public Map<String, String> getCode(HttpServletRequest request,HttpServletResponse response) throws Exception {
        System.out.println("-------------------getCode Start---------------");
        Map<String, String> map  = new HashMap<String, String>();
        String email = request.getParameter("email")==null?"":request.getParameter("email");
        boolean isEmail  = StringUtils.checkEmail(email);
        if(!isEmail){
        	map.put("state", "0");
            map.put("msg", "The email address format error!");
        }else{
        	Mdm mdm = mdmService.getMdmByHql("from Mdm where email = ? ", email);
        	if(null != mdm){
        		 map.put("code", mdm.getDeviceId());
        		 String mobileconfig = MessageFormat.format(ConfigUtils.getConfig("DOWN_MOBILECONFIG"),mdm.getDeviceId());
        		 /**生成二维码**/
                 String imgPath = DateUtils.formatDate(new Date()) + ".png";
//                 String codePath = request.getSession().getServletContext().getRealPath("/qrimg")+ "/" + imgPath;
//         		 TwoDimensionCode handler = new TwoDimensionCode();
//         		 handler.encoderQRCode(mobileconfig, codePath, "png");
         		 map.put("qrimg", "qrimg/"+imgPath);
                 map.put("mobileconfig", mobileconfig);
        	}else{
        		String uuid = UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
                mdm = new Mdm();
                mdm.setId(uuid);
                mdm.setDeviceId(uuid);
                mdm.setEmail(email);
                mdm.setCreateTime(new Timestamp(new Date().getTime()));
                mdmService.save(mdm);
                String mobileconfig = MessageFormat.format(ConfigUtils.getConfig("DOWN_MOBILECONFIG"),uuid);
                /**生成二维码**/
                String imgPath = DateUtils.formatDate(new Date()) + ".png";
                String codePath = request.getSession().getServletContext().getRealPath("/qrimg")+ "/" + imgPath;
        		TwoDimensionCode handler = new TwoDimensionCode();
        		handler.encoderQRCode(mobileconfig, codePath, "png");
                map.put("code", uuid);
                map.put("mobileconfig", mobileconfig);
                map.put("qrimg", "qrimg/"+imgPath);
        	}
        	System.out.println("-------------------getCode End---------------");
        }
        return map;
    }
	
	/**
     * 设备认证和注册功能
     * @throws Exception
     */
	@RequestMapping(value="/checkin/{deviceId}",method=RequestMethod.PUT)
    public void checkIn(@PathVariable("deviceId") String deviceId,HttpServletRequest request,HttpServletResponse response) throws Exception {
        /**获取当期设备的编号和设备信息**/
        Mdm mdm = mdmService.getMdmByHql("from Mdm where deviceId = ? ", deviceId);
        String info = MdmUtils.inputStream2String(request.getInputStream());
        /**Device认证方法调用、Device回传Token方法调用**/
        if (info.toString().contains(MdmUtils.Authenticate)) {
            System.out.println("-------------------Authenticate start---------------");
            System.out.println("Device->Server Authenticate:\n"+info.toString());
            /**保存返回的Token、PushMagic数据**/
            Map<String, String> plistMap = MdmUtils.parseAuthenticate(info.toString());
            String Topic = plistMap.get(MdmUtils.Topic);
            String UDID = plistMap.get(MdmUtils.UDID);
            if (mdm == null) { mdm = new Mdm();}
            mdm.setDeviceId(deviceId);
            mdm.setUdid(UDID);
            mdm.setTopic(Topic);
            mdm.setControl("1");
            mdmService.saveOrUpdtae(mdm);
            /**返回一个空的pList格式的文件**/
            String blankPList = MdmUtils.getBlankPList();
            System.out.println("Server->Device:\n"+blankPList);
            response.setHeader("content-type", "application/xml;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            String configTitle = "MDMApp_EraseDevice";
            response.setHeader("Content-Disposition", "attachment; filename=" + configTitle + ".plist");
            PrintWriter sos = response.getWriter();
            System.out.println("-------------------Authenticate end---------------");
            sos.write(blankPList);
            sos.flush();
            sos.close();
        } else if (info.toString().contains(MdmUtils.TokenUpdate)) {
            System.out.println("-------------------TokenUpdate start---------------");
            System.out.println("Device->Server TokenUpdate:\n"+info.toString());
            /**保存返回的数据**/
            Map<String, String> plistMap = MdmUtils.parseTokenUpdate(info.toString());
            String UnlockToken = MdmUtils.parseUnlockToken(info.toString());
            String UDID = plistMap.get(MdmUtils.UDID);
            String Topic = plistMap.get(MdmUtils.Topic);
            String OriToken = plistMap.get(MdmUtils.Token);
            String PushMagic = plistMap.get(MdmUtils.PushMagic);
            if (mdm == null) { mdm = new Mdm(); }
            mdm.setDeviceId(deviceId);
            mdm.setUdid(UDID);
            mdm.setTopic(Topic);
            mdm.setControl("2");
            mdm.setUnlockToken(UnlockToken);
            /**组装新的Token数据**/
            String Token = MdmUtils.parseToken(OriToken);
            mdm.setToken(Token);
            mdm.setPushMagic(PushMagic);
            mdmService.saveOrUpdtae(mdm);
            /**空返回**/
            System.out.println("Server->Device:\n The HTTP state 200, the content is empty");
            System.out.println("-------------------TokenUpdate end---------------");
            response.setContentType("text/plain;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            PrintWriter out;
            try {
                out = response.getWriter();
                out.print("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (info.toString().contains(MdmUtils.CheckOut)) {
            System.out.println("Device->Server CheckOut:\n"+info.toString());
            System.out.println("-------------------CheckOut start---------------");
            if (mdm != null) {
                mdm.setControl("-1");
                mdmService.saveOrUpdtae(mdm);
            }
            System.out.println("Server->Device:\n Don't need to return");
            System.out.println("-------------------CheckOut end---------------");
        }
    }


    /**
     * 操作状态回执
     * @throws Exception
     */
    @RequestMapping(value="/server/{deviceId}",method=RequestMethod.PUT)
    public void serverUrl(@PathVariable("deviceId") String deviceId,HttpServletRequest request,HttpServletResponse response) throws Exception {
        /**获取当期设备的编号**/
        Mdm mdm = mdmService.getMdmByHql("from Mdm where deviceId = ? ", deviceId);
        String info = MdmUtils.inputStream2String(request.getInputStream());
        System.out.println("Device->Server:\n"+info.toString());
        /**设备空闲状态,可以发送相关命令**/
        if (info.contains(MdmUtils.Idle)) {
            /**执行命令**/
            Command command = (Command)commandService.getCommandByHql("from Command where deviceId=? and doIt=? order by createTime asc", deviceId, "0");
            if (command != null) {
                if (command.getCommand().equals(MdmUtils.Lock)) {
                    System.out.println("-------------------DeviceLock Start---------------");
                    /**发送锁屏命令**/
                    String commandString = MdmUtils.getCommandPList(MdmUtils.Lock, command.getId());
                    command.setDoIt("1");
                    commandService.saveOrUpdate(command);
                    System.out.println("Server->Device Lock:\n"+commandString);
                    response.setHeader("content-type", "application/xml;charset=UTF-8");
                    response.setCharacterEncoding("UTF-8");
                    String configTitle = "MDMApp_DeviceLock";
                    response.setHeader("Content-Disposition", "attachment; filename=" + configTitle + ".plist");
                    PrintWriter sos = response.getWriter();
                    System.out.println("-------------------DeviceLock End---------------");
                    sos.write(commandString);
                    sos.flush();
                    sos.close();
                } else if (command.getCommand().equals(MdmUtils.Erase)) {
                    System.out.println("-------------------EraseDevice Start---------------");
                    /**发送清除谁命令**/
                    String commandString = MdmUtils.getCommandPList(MdmUtils.Erase, command.getId());
                    System.out.println("Server->Device Erase:\n"+commandString);
                    command.setDoIt("1");
                    commandService.saveOrUpdate(command);
                    response.setHeader("content-type", "application/xml;charset=UTF-8");
                    response.setCharacterEncoding("UTF-8");
                    String configTitle = "MDMApp_EraseDevice";
                    response.setHeader("Content-Disposition", "attachment; filename=" + configTitle + ".plist");
                    PrintWriter sos = response.getWriter();
                    System.out.println("-------------------EraseDevice End---------------");
                    sos.write(commandString);
                    sos.flush();
                    sos.close();
                } else if (command.getCommand().equals(MdmUtils.Info)) {
                    System.out.println("-------------------DeviceInformation Start---------------");
                    /**发送获取设备信息命令**/
                    String commandString = MdmUtils.getCommandInfoPList(MdmUtils.Info, command.getId());
                    System.out.println("Server->Device DeviceInformation:\n"+commandString);
                    command.setDoIt("1");
                    commandService.saveOrUpdate(command);
                    response.setHeader("content-type", "application/xml;charset=UTF-8");
                    response.setCharacterEncoding("UTF-8");
                    String configTitle = "MDMApp_DeviceInformation";
                    response.setHeader("Content-Disposition", "attachment; filename=" + configTitle + ".plist");
                    PrintWriter sos = response.getWriter();
                    System.out.println("-------------------DeviceInformation End---------------");
                    sos.write(commandString);
                    sos.flush();
                    sos.close();
                } else if (command.getCommand().equals(MdmUtils.Apps)) {
                    System.out.println("-------------------InstalledApplicationList Start---------------");
                    /**发送获取设备信息命令**/
                    String commandString = MdmUtils.getCommandPList(MdmUtils.Apps, command.getId());
                    System.out.println("Server->Device InstalledApplicationList:\n"+commandString);
                    command.setDoIt("1");
                    commandService.saveOrUpdate(command);
                    response.setHeader("content-type", "application/xml;charset=UTF-8");
                    response.setCharacterEncoding("UTF-8");
                    String configTitle = "MDMApp_InstalledApplicationList";
                    response.setHeader("Content-Disposition", "attachment; filename=" + configTitle + ".plist");
                    PrintWriter sos = response.getWriter();
                    System.out.println("-------------------InstalledApplicationList End---------------");
                    sos.write(commandString);
                    sos.flush();
                    sos.close();
                } else if (command.getCommand().equals(MdmUtils.Clear)) {
                    System.out.println("-------------------ClearPasscode Start---------------");
                    /**发送清除设备密码命令**/
                    String commandString = MdmUtils.getClearPassCodePList(MdmUtils.Clear, command.getId(),mdm);
                    System.out.println("Server->Device ClearPasscode:\n"+commandString);
                    command.setDoIt("1");
                    commandService.saveOrUpdate(command);
                    response.setHeader("content-type", "application/xml;charset=UTF-8");
                    response.setCharacterEncoding("UTF-8");
                    String configTitle = "MDMApp_ClearPasscode";
                    response.setHeader("Content-Disposition", "attachment; filename=" + configTitle + ".plist");
                    PrintWriter sos = response.getWriter();
                    System.out.println("-------------------ClearPasscode End---------------");
                    sos.write(commandString);
                    sos.flush();
                    sos.close();
                } else if (command.getCommand().equals(MdmUtils.Online)) {
                    /**查询设备状态，如果又返回，则修改状态就行了：1 表示设备在线**/
                    command.setDoIt("1");
                    commandService.saveOrUpdate(command);
                } else if (command.getCommand().equals(MdmUtils.Repay)) {
                    /**查询设备状态，如果又返回，则修改状态就行了：1 表示设备在线**/
                    command.setDoIt("1");
                    commandService.saveOrUpdate(command);
                    /**更新设备是否可控状态**/
                    mdm.setControl("2");
                    mdmService.saveOrUpdtae(mdm);
                }
            }
        } else if (info.contains(MdmUtils.Acknowledged)) {
            if (info.contains(MdmUtils.QueryResponses)) {
                System.out.println("-------------------DeviceInformation Start---------------");
                System.out.println("Device->Server DeviceInformation:\n"+info.toString());
                Map<String, String> plistMap = MdmUtils.parseInformation(info);
                System.out.println(plistMap.get("IMEI"));
                String CommandUUID = plistMap.get("CommandUUID");
                Command command = commandService.getCommandById(CommandUUID);
                if (command != null) {
                    command.setResult(MdmUtils.Acknowledged);
                    command.setDoIt("2");
                    commandService.saveOrUpdate(command);
                }
                System.out.println("-------------------DeviceInformation End---------------");
            }else if(info.contains(MdmUtils.InstalledApplicationList)) {
                System.out.println("-------------------InstalledApplicationList Start---------------");
                System.out.println("Device->Server InstalledApplicationList:\n"+info.toString());
                Map<String, Map<String, String>> plistMap = MdmUtils.parseInstalledApplicationList(info);
                String CommandUUID = plistMap.get(MdmUtils.InstalledApplicationList).get("CommandUUID");
                /**保存处理后的APP列表数据 start**/
                for (String key : plistMap.keySet()) {
                    if(!MdmUtils.InstalledApplicationList.equals(key)){
                        Map<String, String> map =  plistMap.get(key);
                        System.out.println("Name:" + map.get("Name"));
                    }
                }
                /**保存处理后的APP列表数据 end**/
                Command command = commandService.getCommandById(CommandUUID);
                if (command != null) {
                    command.setResult(MdmUtils.InstalledApplicationList);
                    command.setDoIt("2");
                    commandService.saveOrUpdate(command);
                }
                System.out.println("-------------------InstalledApplicationList End---------------");
            } else {
                System.out.println("-------------------OtherResult Start---------------");
                Map<String, String> plistMap = MdmUtils.parseCommand(info);
                String CommandUUID = plistMap.get("CommandUUID");
                Command command = commandService.getCommandById(CommandUUID);
                if (command != null) {
                    command.setResult(MdmUtils.Acknowledged);
                    command.setDoIt("2");
                    commandService.saveOrUpdate(command);
                }
                System.out.println("-------------------OtherResult End---------------");
            }
        } else if (info.contains(MdmUtils.CommandFormatError)) {
            System.out.println("-------------------CommandFormatError Start---------------");
            Map<String, String> plistMap = MdmUtils.parseCommand(info);
            String CommandUUID = plistMap.get("CommandUUID");
            Command command = commandService.getCommandById(CommandUUID);
            if (command != null) {
                command.setResult("CommandFormatError");
                command.setDoIt("3");
                commandService.saveOrUpdate(command);
            }
            System.out.println("-------------------CommandFormatError End---------------");
        } else if (info.contains(MdmUtils.Error)) {
            System.out.println("-------------------Error Start---------------");
            Map<String, String> plistMap = MdmUtils.parseCommand(info);
            String CommandUUID = plistMap.get("CommandUUID");
            Command command = commandService.getCommandById(CommandUUID);
            if (command != null) {
                command.setResult("Error");
                command.setDoIt("3");
                commandService.saveOrUpdate(command);
            }
            System.out.println("-------------------Error End---------------");
        } else if (info.contains(MdmUtils.NotNow)) {
            System.out.println("-------------------NotNow Start---------------");
            Map<String, String> plistMap = MdmUtils.parseCommand(info);
            String CommandUUID = plistMap.get("CommandUUID");
            Command command = commandService.getCommandById(CommandUUID);
            if (command != null) {
                command.setResult("NotNow");
                commandService.saveOrUpdate(command);
            }
            System.out.println("-------------------NotNow End---------------");
        }
    }
    

    /**
     * 设备锁屏功能
     * @throws Exception
     */
    @ResponseBody
	@RequestMapping("/lock/{deviceId}")
    public Map<String, String> deviceLock(@PathVariable("deviceId") String deviceId,HttpServletRequest request,HttpServletResponse response) throws Exception {
        System.out.println("-------------------Lock Start---------------");
        Map<String, String> map = new HashMap<String, String>();
        Mdm mdm = mdmService.getMdmByHql("from Mdm where deviceId = ? ", deviceId);
        if(null==mdm){
        	map.put("state", "0");
            map.put("msg", "Incorrect code!");
        }else{
             /**对设备进行锁屏**/
             String pemFile = ConfigUtils.getConfig("APNS_P12MDM");
             String pemPath = request.getSession().getServletContext().getRealPath("/mdmtool")+ "/" + pemFile;
             int pushState = PushUtils.singleMDMPush(pemPath, mdm);
             if (pushState == 1) {
                 Command command = new Command();
                 command.setCommand(MdmUtils.Lock);
                 command.setDeviceId(deviceId);
                 command.setDoIt("0");
                 commandService.saveOrUpdate(command);
                 map.put("state", "1");
                 map.put("msg", "send lock command success!");
             } else {
                 map.put("state", "0");
                 map.put("msg", "send lock command failure!");
             }
             System.out.println("-------------------Lock End---------------");
        }
        return map;
    }

    /**
     * 擦除设备数据功能
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/erase/{deviceId}")
    public Map<String, String> deviceErase(@PathVariable("deviceId") String deviceId,HttpServletRequest request,HttpServletResponse response) throws Exception {
        System.out.println("-------------------Erase Start---------------");
        Map<String, String> map = new HashMap<String, String>();
        Mdm mdm = mdmService.getMdmByHql("from Mdm where deviceId = ? ", deviceId);
        if(null==mdm){
        	map.put("state", "0");
            map.put("msg", "Incorrect code!");
        }else{
            /**对设备进行锁屏**/
            String pemFile = ConfigUtils.getConfig("APNS_P12MDM");
            String pemPath = request.getSession().getServletContext().getRealPath("/mdmtool")+"/" + pemFile;
            int pushState = PushUtils.singleMDMPush(pemPath, mdm);
            if (pushState == 1) {
                Command command = new Command();
                command.setCommand(MdmUtils.Erase);
                command.setDeviceId(deviceId);
                command.setDoIt("0");
                commandService.saveOrUpdate(command);
                map.put("state", "1");
                map.put("msg", "send erase command success!");
            } else {
                map.put("state", "0");
                map.put("msg", "send erase command failure!");
            }
            System.out.println("-------------------Erase End---------------");
        }
        return map;
    }

    /**
     * 获取设备信息
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/info/{deviceId}")
    public Map<String, String> deviceInformation(@PathVariable("deviceId") String deviceId,HttpServletRequest request,HttpServletResponse response) throws Exception {
        System.out.println("-------------------Information Start---------------");
        Map<String, String> map = new HashMap<String, String>();
        Mdm mdm = mdmService.getMdmByHql("from Mdm where deviceId = ? ", deviceId);
        if(null==mdm){
        	map.put("state", "0");
            map.put("msg", "Incorrect code!");
        }else{
            /**对设备进行锁屏**/
            String pemFile = ConfigUtils.getConfig("APNS_P12MDM");
            String pemPath = request.getSession().getServletContext().getRealPath("/mdmtool")+ "/" + pemFile;
            int pushState = PushUtils.singleMDMPush(pemPath, mdm);
            if (pushState == 1) {
                Command command = new Command();
                command.setCommand(MdmUtils.Info);
                command.setDeviceId(deviceId);
                command.setDoIt("0");
                commandService.saveOrUpdate(command);
                map.put("state", "1");
                map.put("msg", "query device information command success!");
            } else {
                map.put("state", "0");
                map.put("msg", "query device information command failure!");
            }
            System.out.println("-------------------Information End---------------");
        }
        return map;
    }

    /**
     * 清除设备密码功能
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/clear/{deviceId}")
    public Map<String, String> clearPasscode(@PathVariable("deviceId") String deviceId,HttpServletRequest request,HttpServletResponse response) throws Exception {
        System.out.println("-------------------ClearPasscode Start---------------");
        Map<String, String> map = new HashMap<String, String>();
        Mdm mdm = mdmService.getMdmByHql("from Mdm where deviceId = ? ", deviceId);
        if(null==mdm){
        	map.put("state", "0");
            map.put("msg", "Incorrect code!");
        }else{
             /**对设备进行清除密码**/
             String pemFile = ConfigUtils.getConfig("APNS_P12MDM");
             String pemPath = request.getSession().getServletContext().getRealPath("/mdmtool")+ "/" + pemFile;
             int pushState = PushUtils.singleMDMPush(pemPath, mdm);
             if (pushState == 1) {
                 Command command = new Command();
                 command.setCommand(MdmUtils.Clear);
                 command.setDeviceId(deviceId);
                 command.setDoIt("0");
                 commandService.saveOrUpdate(command);
                 map.put("state", "1");
                 map.put("msg", "clear pass code command success!");
             } else {
                 map.put("state", "0");
                 map.put("msg", "clear pass code command failure!");
             }
             System.out.println("-------------------ClearPasscode End---------------");
        }
        return map;
       
    }

    /**
     * 获取设备已经安装的app信息
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/apps/{deviceId}")
    public Map<String, String> deviceInstalledApplicationList(@PathVariable("deviceId") String deviceId,HttpServletRequest request,HttpServletResponse response) throws Exception {
        System.out.println("-------------------InstalledApplicationList Start---------------");
        Map<String, String> map = new HashMap<String, String>();
        Mdm mdm = mdmService.getMdmByHql("from Mdm where deviceId = ? ", deviceId);
        if(null==mdm){
        	map.put("state", "0");
            map.put("msg", "Incorrect code!");
        }else{
            /**对设备进行锁屏**/
            String pemFile = ConfigUtils.getConfig("APNS_P12MDM");
            String pemPath = request.getSession().getServletContext().getRealPath("/mdmtool")+ "/" + pemFile;
            int pushState = PushUtils.singleMDMPush(pemPath, mdm);
            if (pushState == 1) {
                Command command = new Command();
                command.setCommand(MdmUtils.Apps);
                command.setDeviceId(deviceId);
                command.setDoIt("0");
                commandService.saveOrUpdate(command);
                map.put("state", "1");
                map.put("msg", "get installed application list command success!");
            } else {
                map.put("state", "0");
                map.put("msg", "get installed application list command failure!");
            }
            System.out.println("-------------------InstalledApplicationList End---------------");
        }
        return map;
    }

    /**
     * 下载设备控制描述文件功能
     * @throws Exception
     */
    @RequestMapping("/down/{deviceId}")
    public void downConfig(@PathVariable("deviceId") String deviceId,HttpServletRequest request,HttpServletResponse response) throws Exception {
        System.out.println("-------------------Download MobileConfig File Start---------------");
        Mdm mdm = mdmService.getMdmByHql("from Mdm where deviceId = ? ", deviceId);
        if(null==mdm){
        	Map<String, String> map = new HashMap<String, String>();
        	map.put("state", "0");
            map.put("msg", "Incorrect code!");
            String jsonObject = JSON.toJSONString(map);
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            PrintWriter out;
            try {
                out = response.getWriter();
                out.print(jsonObject);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
        	/**生成签名的MobileConfig配置文件的三个证书文件**/
            System.out.println("----------------------生成证书文件等的路径 start---------------------");
            String configPath =  request.getSession().getServletContext().getRealPath("/mdmtool");
            String tempPath =  request.getSession().getServletContext().getRealPath("/mdmtool")+"/down";
            String crtPath = configPath + ConfigUtils.getConfig("APNS_CRT");
            String keyPath = configPath + ConfigUtils.getConfig("APNS_KEY");
            String pemPath = configPath + ConfigUtils.getConfig("APNS_PEM");
            System.out.println("----------------------生成证书文件等的路径 end---------------------");
            /**创建未签名的文件和已签名的MobileConfig文件**/
            System.out.println("----------------------生成未签名的mobileconfig文件 start---------------------");
            String oldPath =  tempPath + "/" + deviceId + ".mobileconfig";
            String newPath =  tempPath + "/" + deviceId + "Signed.mobileconfig";
            String content = MdmUtils.readConfig(configPath).replace("#deviceId#", deviceId);
            boolean createSuccess = MdmUtils.createMobileConfigFile(oldPath,content);
            System.out.println("----------------------生成未签名的mobileconfig文件 end---------------------");
            /**签名和认证过程**/
            if(createSuccess){
                System.out.println("----------------------签名mobileconfig文件 start---------------------");
                String oldCmd = " openssl smime -sign -in {0} -out {1} -signer {2} -inkey {3} -certfile {4} -outform der -nodetach ";
                String newCmd = MessageFormat.format(oldCmd,oldPath,newPath,crtPath,keyPath,pemPath);
                System.out.println("OpenSSL：\n" + newCmd);
                Runtime.getRuntime().exec("cmd /c "+newCmd);
                System.out.println("----------------------签名mobileconfig文件 end---------------------");
            }
            System.out.println("----------------------下载签名后的mobileconfig文件 start---------------------");
            response.setHeader("content-type", "application/xml;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            String configTitle = "MDMApp_"+deviceId;
            response.setHeader("Content-Disposition", "attachment; filename=" + configTitle + ".mobileconfig");
            /**获取配置文件动态组装参数**/
            System.out.println("----------------------下载签名后的mobileconfig文件 end---------------------");
            try {
                Thread.sleep(2000);// 括号里面的5000代表5000毫秒，也就是5秒，可以该成你需要的时间
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            /**写入文件**/
            java.io.File f = new java.io.File(newPath);
            while(true){
                if(f.exists() && f.length()>0){
                    java.io.FileInputStream fis = new java.io.FileInputStream(newPath);
                    java.io.OutputStream os = response.getOutputStream();
                    byte[] b = new byte[1024];
                    int i = 0;
                    while ((i = fis.read(b)) > 0) { os.write(b, 0, i); }
                    fis.close();
                    os.flush();
                    os.close();
                    break;
                }else{
                   continue;
                }
            }
        }
    }
    
    @RequestMapping("/testdown")
    public void down(HttpServletRequest request,HttpServletResponse response) throws Exception {
        response.setHeader("content-type", "application/xml;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        String configTitle = "MDMApp";
        response.setHeader("Content-Disposition", "attachment; filename=" + configTitle + ".mobileconfig");
        String tempPath =  request.getSession().getServletContext().getRealPath("/mdmtool")+"/down/";
            /**写入文件**/
            java.io.File f = new java.io.File(tempPath+"HelyMDMS.mobileconfig");
            while(true){
                if(f.exists() && f.length()>0){
                    java.io.FileInputStream fis = new java.io.FileInputStream(tempPath+"HelyMDMS.mobileconfig");
                    java.io.OutputStream os = response.getOutputStream();
                    byte[] b = new byte[1024];
                    int i = 0;
                    while ((i = fis.read(b)) > 0) { os.write(b, 0, i); }
                    fis.close();
                    os.flush();
                    os.close();
                    break;
                }else{
                   continue;
                }
            }
    }
    
    
    public static void main(String[] args) {
        
    }
	
	/****************************************************************/
	public MdmService getMdmService() {
		return mdmService;
	}
	@Resource
	public void setMdmService(MdmService mdmService) {
		this.mdmService = mdmService;
	}
	
	public CommandService getCommandService() {
		return commandService;
	}
	@Resource
	public void setCommandService(CommandService commandService) {
		this.commandService = commandService;
	}
	
	
}
