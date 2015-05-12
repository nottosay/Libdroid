package sample.libdroid;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.util.libdroid.DBManager;
import com.util.libdroid.ViewInjector;
import com.util.libdroid.view.annotation.ContentView;
import com.util.libdroid.view.annotation.Find;

import java.util.ArrayList;
import java.util.List;

@ContentView(R.layout.activity_my)
public class MyActivity extends Activity {

    @Find(R.id.tv_text1)
    TextView tv_text1;

    @Find(R.id.lv_date)
    ListView mTvUserId;

    private MyAdapter mAdapter;
    private List<UserModel> userModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewInjector.inject(this);

        tv_text1.setText("injext text");

        userModels = new ArrayList<UserModel>();
        mAdapter = new MyAdapter(this,userModels);

        mTvUserId.setAdapter(mAdapter);
        new insertData().execute();
    }

    class insertData extends AsyncTask<String,String,UserModel>{
        @Override
        protected UserModel doInBackground(String... params) {
           UserModel userModel = new UserModel();
           userModel.userId = "test1";
           userModel.userName = "测试1";
           userModel.save();
            UserModel userMode2 = new UserModel();
            userMode2.userId = "test2";
            userMode2.userName = "测试2";
            userMode2.save();
            UserModel userMode3 = new UserModel();
            userMode3.userId = "test3";
            userMode3.userName = "测试3";
            userMode3.save();
            UserModel userMode4 = new UserModel();
            userMode4.userId = "test4";
            userMode4.userName = "测试4";
            userMode4.save();
            UserModel userMode5 = new UserModel();
            userMode5.userId = "test5";
            userMode5.userName = "测试5";
            userMode5.save();
           return null;
        }

        @Override
        protected void onPostExecute(UserModel userModel) {
            super.onPostExecute(userModel);

            mAdapter.setData(DBManager.loadAll(UserModel.class));
        }
    }


    class MyAdapter extends BaseAdapter{

        private List<UserModel> userModels;
        private LayoutInflater inflater;

        public MyAdapter(Context context,List<UserModel> userModels){
            inflater = LayoutInflater.from(context);
            this.userModels = userModels;
        }

        public void setData(List<UserModel> userModels){
            this.userModels = userModels;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return userModels.size();
        }

        @Override
        public Object getItem(int position) {
            return userModels.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null){
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_user,parent,false);
                ViewInjector.inject(viewHolder,convertView);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder)convertView.getTag();
            }

            UserModel model = userModels.get(position);
            viewHolder.tvUserId.setText(model.userId);
            viewHolder.tvUserName.setText(model.userName);
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class ViewHolder{
            @Find(R.id.tv_userId)
            TextView tvUserId;
            @Find(R.id.tv_userName)
            TextView tvUserName;
        }
    }
}
