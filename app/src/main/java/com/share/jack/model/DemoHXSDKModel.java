
package com.share.jack.model;

import android.content.Context;

import com.share.jack.db.DemoDBManager;
import com.share.jack.db.UserDao;
import com.share.jack.domain.RobotUser;
import com.share.jack.domain.User;

import java.util.List;
import java.util.Map;

public class DemoHXSDKModel extends DefaultHXSDKModel{

    public DemoHXSDKModel(Context ctx) {
        super(ctx);
    }

    public boolean getUseHXRoster() {
        return true;
    }
    
    // demo will switch on debug mode
    public boolean isDebugMode(){
        return true;
    }
    
    public boolean saveContactList(List<User> contactList) {
        UserDao dao = new UserDao(context);
        dao.saveContactList(contactList);
        return true;
    }

    public Map<String, User> getContactList() {
        UserDao dao = new UserDao(context);
        return dao.getContactList();
    }
    
    public void saveContact(User user){
    	UserDao dao = new UserDao(context);
    	dao.saveContact(user);
    }
    
    public Map<String, RobotUser> getRobotList(){
    	UserDao dao = new UserDao(context);
    	return dao.getRobotUser();
    }

    public boolean saveRobotList(List<RobotUser> robotList){
    	UserDao dao = new UserDao(context);
    	dao.saveRobotUser(robotList);
    	return true;
    }
    
    public void closeDB() {
        DemoDBManager.getInstance().closeDB();
    }
    
    @Override
    public String getAppProcessName() {
        return context.getPackageName();
    }
}
