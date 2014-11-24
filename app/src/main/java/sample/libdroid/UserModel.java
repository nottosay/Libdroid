package sample.libdroid;

import com.util.libdroid.db.Model;
import com.util.libdroid.db.annotation.Column;
import com.util.libdroid.db.annotation.Table;

/**
 * Created by wally.yan on 2014/11/21.
 */
//@Table(name = "table_user")
public class UserModel extends Model {

    @Column
    public String userId;

    @Column
    public String userName;
}
