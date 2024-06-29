package org.exthmui.microlauncher.duoqin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.exthmui.microlauncher.duoqin.R;

import es.dmoral.toasty.Toasty;

public class MenuActivity extends AppCompatActivity{
    private final static int[] title = { R.string.menu_set_wallpaper,R.string.menu_settings_system,R.string.menu_settings_launcher,R.string.menu_start,R.string.menu_about_me};
    private final static int[] summary = {R.string.menu_set_wallpaper_sum,R.string.menu_settings_system_sum,R.string.menu_settings_launcher_sum,R.string.menu_start_sum,R.string.menu_about_sum};
    private final int[] icon = {R.drawable.ic_wallpaper,R.drawable.ic_settings_system,R.drawable.ic_settings_launcher,R.drawable.ic_start,R.drawable.ic_home};
    Intent it = new Intent();
    TextView back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_view);
        ListView menu_view = findViewById(R.id.menu_list);
        back=findViewById(R.id.menu_back);
        //创建一个Adapter的实例
        MyBaseAdapter mAdapter = new MyBaseAdapter();
        //设置Adapter
        menu_view.setAdapter(mAdapter);
        menu_view.setOnItemClickListener(new mItemClick());
        ActionBar actionBar = this.getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        back.setOnClickListener(v -> finish());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // When the home button is pressed, take the user back to the MainActivity
        if (id == android.R.id.home) {
            //NavUtils.navigateUpFromSameTask(this);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //创建一个类继承BaseAdapter
    class MyBaseAdapter extends BaseAdapter {
        //得到item的总数
        @Override
        public int getCount() {
            //返回ListView Item条目的总数
            return title.length;
        }
        //得到Item代表的对象
        @Override
        public Object getItem(int position) {
            //返回ListView Item条目代表的对象
            return title[position];
        }
        //得到Item的id
        @Override
        public long getItemId(int position) {
            //返回ListView Item的id
            return position;
        }
        //得到Item的View视图
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            //使用了懒汉模式
            if(convertView == null){
                //将list_item.xml文件找出来并转换成View对象
                convertView  = View.inflate(MenuActivity.this, R.layout.list_item, null);
                //找到list_item.xml中创建的TextView
                holder = new ViewHolder();
                holder.title = convertView.findViewById(R.id.menu_title);
                holder.summary = convertView.findViewById(R.id.menu_summary);
                holder.icon = convertView.findViewById(R.id.menu_icon);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            holder.title.setText(title[position]);
            holder.summary.setText(summary[position]);
            holder.icon.setBackgroundResource(icon[position]);
            return convertView;
        }
    }
    static class ViewHolder{
        TextView title;
        TextView summary;
        ImageView icon;
    }
    class mItemClick implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (Math.toIntExact(id)){
                case 0:
                    Toast.makeText(MenuActivity.this,R.string.choose_wallpaper,Toast.LENGTH_LONG).show();
                    final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
                    Intent chooser = Intent.createChooser(pickWallpaper,"选择一个壁纸设置...\nSetup wallpaper...");
                    //发送设置壁纸的请求
                    startActivity(chooser);
                    break;
                case 1:
                    it.setClassName("com.android.settings",
                            "com.android.settings.Settings");
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(it);
                    break;
                case 2:
                    //Toasty.info(MenuActivity.this,R.string.what,Toast.LENGTH_LONG,true).show();
                    Intent menu_it = new Intent(MenuActivity.this, SettingsActivity.class);
                    startActivity(menu_it);
                    break;
                case 3:
                    Toasty.info(MenuActivity.this,R.string.what,Toast.LENGTH_LONG,true).show();
                    break;
                case 4:
                    Intent about_it = new Intent(MenuActivity.this, AboutActivity.class);
                    about_it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(about_it);
                    break;
            }
        }
    }
}
