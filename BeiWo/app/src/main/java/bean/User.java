package bean;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobRelation;

/**
 * Created by huang.fan on 2016-3-21.
 */
public class User extends BmobUser {
    private static final long serialVersionUID = 1L;
    // 头像
    private BmobFile avatar ;
    /* 昵称 */
    private String nickname;
    /* 个性签名 */
    private String sign;
    /* 性别 0：男，1：女 */
    private Integer sex;
    /* 年龄 */
    private Integer age;
    /* 粉丝 */
    private BmobRelation fans ;
    /* 所关注人 */
    private BmobRelation follow ;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public BmobFile getAvatar() {
        return avatar;
    }

    public void setAvatar(BmobFile avatar) {
        this.avatar = avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public BmobRelation getFans() {
        return fans;
    }

    public void setFans(BmobRelation fans) {
        this.fans = fans;
    }

    public BmobRelation getFollow() {
        return follow;
    }

    public void setFollow(BmobRelation follow) {
        this.follow = follow;
    }
}
