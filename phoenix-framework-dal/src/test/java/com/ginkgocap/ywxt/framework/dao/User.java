package com.ginkgocap.ywxt.framework.dao;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.context.annotation.Lazy;

@Entity
@Table(name = "vctl_user")

public class User implements Serializable {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}

	public static final String SPLITER = "_";
	public static final String CHINA = "0086";
	/**
     *
     */
	private static final long serialVersionUID = -8589122869401129687L;

	private Long userId;
	private String mobileNo;
	private String passport;
	private String password; // 密码
	private String origin; // 账号来源 1-weaver 0-e2e，9美拍
	private Long createAt;
	private String ip;
	private Long updateAt;
	private Boolean confirm; // 账号是否经过用户确认 0-未认证，1-已认证
	private byte status = 0; // 0，正常 ， 1.冻结
	private Long lasId; // LPS Authentication Service AccountId
	private String picUrl;
	private String realName;
	private Boolean isTempAccount;
	private Integer firstLogin;// 是否首次登陆
	private String pinyin; // 汉语拼音
	private String thirdId;// 第三方应用的id 支持 oauth登录使用 比如 sina 用户id 或者 qq的 openid
	private String appKey;
	private String appSecret;

	private Short gender;
	private String oemTag;
	private String errorCode;
	private String countryCode;
	private String errorInfo;
	private String oldMobileNo;// 以前绑定的电话号码
	private Long mobileNoOldUserId;// 新绑定的电话号码对应的老用户id

	private String tvMobile;

	/**
	 * 0 没有冻结 -1 永久冻结 正整数 冻结小时数
	 */
	private int freeze;

	private String alias;// 别名 昵称




	public User() {
		super();
	}

	public User(String errorCode) {
		super();
		this.errorCode = errorCode;
	}

	private User(Long userId, String mobileNo, String passport, String password, String origin, Long createAt, String ip, Long updateAt, Boolean confirm, byte status, Long lasId,
			String picUrl, String realName, Boolean isTempAccount, Integer firstLogin, String pinyin, String thirdId, String appKey, String appSecret, Short gender, String oemTag,
			String errorCode) {
		super();
		this.userId = userId;
		this.mobileNo = mobileNo;
		this.passport = passport;
		this.password = password;
		this.origin = origin;
		this.createAt = createAt;
		this.ip = ip;
		this.updateAt = updateAt;
		this.confirm = confirm;
		this.status = status;
		this.lasId = lasId;
		this.picUrl = picUrl;
		this.realName = realName;
		this.isTempAccount = isTempAccount;
		this.firstLogin = firstLogin;
		this.pinyin = pinyin;
		this.thirdId = thirdId;
		this.appKey = appKey;
		this.appSecret = appSecret;
		this.gender = gender;
		this.oemTag = oemTag;
		this.errorCode = errorCode;
	}

	@Column(name = "real_name")
	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	@Column(name = "tv_mobile")
	public String getTvMobile() {
		return tvMobile;
	}

	public void setTvMobile(String tvMobile) {
		this.tvMobile = tvMobile;
	}

	@Id
	@GeneratedValue(generator = "userId")
	@GenericGenerator(name = "userId", strategy = "com.ginkgocap.ywxt.framework.dal.dao.id.util.TimeIdGenerator", parameters = { @Parameter(name = "sequence", value = "contacts_vctl_user") })
	@Column(name = "user_id")
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Column(name = "confirm")
	public Boolean getConfirm() {
		return confirm;
	}

	public void setConfirm(Boolean confirm) {
		this.confirm = confirm;
	}

	@Column(name = "mobile_no")
	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	@Column(name = "passport")
	public String getPassport() {
		return passport;
	}

	public void setPassport(String passport) {
		this.passport = passport;
	}

	@Column(name = "create_at")
	public Long getCreateAt() {
		return createAt;
	}

	public void setCreateAt(Long createAt) {
		this.createAt = createAt;
	}

	@Column(name = "ip")
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	@Column(name = "update_at")
	public Long getUpdateAt() {
		return updateAt;
	}

	public void setUpdateAt(Long updateAt) {
		this.updateAt = updateAt;
	}

	@Column(name = "status")
	public byte getStatus() {
		return status;
	}

	public void setStatus(byte status) {
		this.status = status;
	}

	@Column(name = "las_id")
	public Long getLasId() {
		return lasId;
	}

	public void setLasId(Long lasId) {
		this.lasId = lasId;
	}

	@Column(name = "pic_url")
	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	@Column(name = "is_temp_account")
	public Boolean getIsTempAccount() {
		return isTempAccount;
	}

	public void setIsTempAccount(Boolean isTempAccount) {
		this.isTempAccount = isTempAccount;
	}

	@Column(name = "password")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Column(name = "origin")
	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	@Column(name = "first_login")
	public Integer getFirstLogin() {
		return firstLogin;
	}

	public void setFirstLogin(Integer firstLogin) {
		this.firstLogin = firstLogin;
	}

	@Column(name = "pinyin")
	public String getPinyin() {
		return pinyin;
	}

	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}

	@Column(name = "gender")
	public Short getGender() {
		return this.gender;
	}

	public void setGender(Short gender) {
		this.gender = gender;
	}

	@Transient
	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	@Column(name = "third_id")
	public String getThirdId() {
		return thirdId;
	}

	public void setThirdId(String thirdId) {
		this.thirdId = thirdId;
	}

	@Column(name = "oem_tag")
	public String getOemTag() {
		return oemTag;
	}

	public void setOemTag(String oemTag) {
		this.oemTag = oemTag;
	}

	@Column(name = "alias")
	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Transient
	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	@Transient
	public String getAppSecret() {
		return appSecret;
	}

	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}

	@Transient
	public String getCountryCode() {
		if (countryCode == null) {
			if (mobileNo != null) {
				String[] tmp = mobileNo.split(SPLITER);
				if (tmp != null && tmp.length == 2) {
					countryCode = tmp[0];
					mobileNo = tmp[1];
				} else if (tmp != null && tmp.length == 1) {
					mobileNo = tmp[0];
					countryCode = CHINA;
				}
			}
		}
		return countryCode;
	}

	public void setCountryCode() {
		if (countryCode != null && !countryCode.equals(CHINA)) {
			if (!mobileNo.contains(SPLITER)) {
				mobileNo = countryCode + SPLITER + mobileNo;
				countryCode = null;
			}
		}
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	@Transient
	public String getOldMobileNo() {
		return oldMobileNo;
	}

	public void setOldMobileNo(String oldMobileNo) {
		this.oldMobileNo = oldMobileNo;
	}

	@Transient
	public Long getMobileNoOldUserId() {
		return mobileNoOldUserId;
	}

	public void setMobileNoOldUserId(Long mobileNoOldUserId) {
		this.mobileNoOldUserId = mobileNoOldUserId;
	}

	// @Column(name = "freeze")
	@Transient
	public int getFreeze() {
		return freeze;
	}

	public void setFreeze(int freeze) {
		this.freeze = freeze;
	}

	@Transient
	public String getErrorInfo() {
		return errorInfo;
	}

	public void setErrorInfo(String errorInfo) {
		this.errorInfo = errorInfo;
	}

	@Override
	public String toString() {
		return "User [userId=" + userId + ", mobileNo=" + mobileNo + ", passport=" + passport + ", password=" + password + ", origin=" + origin + ", createAt=" + createAt
				+ ", ip=" + ip + ", updateAt=" + updateAt + ", confirm=" + confirm + ", status=" + status + ", lasId=" + lasId + ", picUrl=" + picUrl + ", realName=" + realName
				+ ", isTempAccount=" + isTempAccount + ", firstLogin=" + firstLogin + ", pinyin=" + pinyin + ", thirdId=" + thirdId + ", appKey=" + appKey + ", appSecret="
				+ appSecret + ", gender=" + gender + ", oemTag=" + oemTag + ", errorCode=" + errorCode + ", countryCode=" + countryCode + ", errorInfo=" + errorInfo
				+ ", oldMobileNo=" + oldMobileNo + ", mobileNoOldUserId=" + mobileNoOldUserId + ", tvMobile=" + tvMobile + ", freeze=" + freeze + ", alias=" + alias + "]";
	}

}
