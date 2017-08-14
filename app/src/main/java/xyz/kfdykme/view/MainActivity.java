package xyz.kfdykme.view;

import android.app.*;
import android.os.*;
import android.support.v7.widget.*;
import java.util.*;
import android.widget.*;
import android.widget.TableRow.*;
import android.view.*;

public class MainActivity extends Activity 
{
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
      	
		//setContentView(R.layout.main);
		
		
		
		List<KfMapData<String>> data = new ArrayList<KfMapData<String>>();
		for(int i =0; i < 9 ; i++)
		{
			data.add(new KfMapData<String>("Course" +i));
		}

		data.get(0).getNex().add(data.get(1));
		data.get(0).getNex().add(data.get(2));

		data.get(1).getNex().add(data.get(3));
		
		data.get(2).getNex().add(data.get(3));
		data.get(2).getNex().add(data.get(4));
		data.get(2).getNex().add(data.get(5));
		
		data.get(4).getNex().add(data.get(6));
		data.get(4).getNex().add(data.get(7));
		data.get(4).getNex().add(data.get(8));
		//LinearLayout ll = (LinearLayout) findViewById(R.id.mainLinearLayout);
		KfMapView map = new KfMapView(this); //(KfMapView) findViewById(R.id.mKfMapView);
		map.setData(data);
		setContentView(map);
		//ll.addView(map,LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		}
		
    
		
}
