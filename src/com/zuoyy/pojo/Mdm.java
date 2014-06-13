package com.zuoyy.pojo;
import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="db_mdm")
public class Mdm implements Serializable {

    private static final long serialVersionUID = -7048625537017892345L;

    @Id
    private String id;
    /**用户邮箱地址**/
    private String email; 
    /**设备编号（和Device主键对应）**/
    private String deviceId; 
    /**用于简单推送发送**/
    private  String pushToken;   
    /**以下是MDM推送相关**/
    private  String topic;
    private  String token;
    private  String pushMagic;
    private  String udid;
    @Column(length = 65535)
    private  String unlockToken;
    /**注册时间**/
    private Timestamp createTime = new Timestamp(System.currentTimeMillis()); 
    /**设备状态（1：已认证；2可控制；-1：已移除）**/
    private String control;

    public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    public String getPushMagic() {
        return pushMagic;
    }

    public void setPushMagic(String pushMagic) {
        this.pushMagic = pushMagic;
    }

    public String getUdid() {
        return udid;
    }

    public void setUdid(String udid) {
        this.udid = udid;
    }

    public String getUnlockToken() {
        return unlockToken;
    }

    public void setUnlockToken(String unlockToken) {
        this.unlockToken = unlockToken;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    public String getControl() {
        return control;
    }

    public void setControl(String control) {
        this.control = control;
    }



}
