package xyz.kfdykme.view;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import java.util.*;
import android.support.v4.widget.*;
import android.graphics.drawable.*;

public class KfMapView extends ViewGroup
{
	int bule1,bule3,bule4,bule5;
	
	int colorBackGrpund, colorLine,colorNode,colorText;
	
	Drawable picBackground, picNode;
	
	List<KfMapData<String>> data;
	
	
	//AttributeSet attrs;
	
	List<KfMapNodeView> dispatchedViews = new ArrayList<KfMapNodeView>();
	
	static final int DEFAULT_MIN_DISTANCE = 300;
	
	int maxDistance = 1000;
	
	int minDistance = DEFAULT_MIN_DISTANCE;//min distance between two node
	
	boolean isLoaded = false;
	
	boolean haStartView = false;
	
	Paint mPaint;
	
	int baseX = 0;
	int baseY = 0;
	
	List<Line> lines = new ArrayList<Line>();
	
	enum DrawStatus{empty,draw};
	
	private DrawStatus drawStatus = DrawStatus.empty;
	
	
	public KfMapView(Context context){
		super(context,null);
		picBackground = getContext().getResources().getDrawable(R.drawable.image_1);

		setBackground(picBackground);

		bule1 = Color.parseColor("#011935");
		colorBackGrpund = Color.parseColor("#00343f");
		bule3 = Color.parseColor("#1db0b8");
		bule4 = Color.parseColor("#37c6c0");
		bule5 = Color.parseColor("#d0e9ff");

		//setBackgroundColor(colorBackGrpund);


		//do scroll
		setOnTouchListener(new OnTouchListener(){

				int lastRawX = 0;
				int lastRawY = 0;
				@Override
				public boolean onTouch(View p1, MotionEvent p2)
				{

					for(int i =0 ; i < getChildCount();i++){
						KfMapNodeView view = (KfMapView.KfMapNodeView) getChildAt(i);
						layout(view);
					}
					int rawX = (int) p2.getRawX();
					int rawY = (int) p2.getRawY();

					int dX = rawX-lastRawX;
					int dY = rawY - lastRawY;
					//Toast.makeText(context,dX+","+dY,Toast.LENGTH_SHORT).show();

					if(Math.abs(dX)<100)
						baseX+=dX;
					if(Math.abs(dY)<100)
						baseY+=dY;
					lastRawX = rawX;
					lastRawY = rawY;


					return true;
				}
			});
	}
	
	

	
	
	
	@Override
	protected void dispatchDraw(Canvas canvas)
	{

		switch(drawStatus){
			case draw:
				mPaint = new Paint();

				mPaint.setColor(bule4);
				mPaint.setAntiAlias(true);
				mPaint.setStrokeWidth(20);
				mPaint.setAlpha(140);

				//spread vies on load
				if(!isLoaded)
					startFirstView((KfMapNodeView)getChildAt(0),canvas);

				//link views by nex
				for(int i = 0; i < getChildCount();i++){
					if(getChildAt(i).getVisibility() == View.VISIBLE){
						KfMapNodeView view = (KfMapView.KfMapNodeView) getChildAt(i);
						//get kfmapdata from view;
						int pos = data.size()-1;
						for(;pos>-1;pos--){
							if(data.get(pos).getData().equals(view.getText().toString()))
								break;
						}
						if(pos == -1) return ;
						KfMapData<String> d = data.get(pos);
						for(int j = 0; j < d.getNex().size();j++){
							KfMapNodeView view2 = findNodeViewByString(d.getNex().get(j).getData());
							if(view2.isOnCurrectPosition)
								drawLine(canvas,view,view2,mPaint);
						}
					}
				}

				//link views by drag & touch
				for(int i =0; i < getChildCount();i++){
					KfMapNodeView view = (KfMapView.KfMapNodeView) getChildAt(i);
					if(view.getState() == KfMapNodeView.TOUCH_STATUS_ON_TOUCH_MOVING){
						for(int j = 0; j < getChildCount();j++){
							KfMapNodeView tView = (KfMapView.KfMapNodeView) getChildAt(j);
							if(tView.getState() == KfMapNodeView.TOUCH_STATUS_ON_TOUCH_DRAGING){
								Paint paint = new Paint();
								paint.setColor(bule5);
								paint.setStrokeWidth(5);
								drawLine(canvas,view,tView,paint);
							}
						}
					}
				}
				
				break;
				
			case empty:
				break;
		}

		super.dispatchDraw(canvas);

		//Toast.makeText(context,"finish",Toast.LENGTH_SHORT).show();
	}
	
	int inter = 0;
	public void drawLine(Canvas canvas,KfMapNodeView view1,KfMapNodeView view2,Paint paint){

//		Line cLine = new Line(view1,view2);
//		for(Line l:lines){
//			if(l.getView1() == view1 && l.getView2() == view2){
//				return;
//			}
//		}
//						
//		lines.add(cLine);
		canvas.drawLine(view1.getRCenter().x+baseX
						,view1.getRCenter().y-view1.getSCenter().y+baseY
						,view2.getRCenter().x+baseX
						,view2.getRCenter().y-view2.getSCenter().y+baseY,paint);
		
	}

	public KfMapNodeView findNodeViewByString(String s){

		for(int i = 0;i < getChildCount();i++){
			KfMapNodeView view = (KfMapNodeView) getChildAt(i);
			if(s.equals(view.getText()))
				return view;
		} 

		return null;
	}
	
	public float getDistance(PointF p1,  PointF p2){
		float d = (p1.x *p2.x)+(p1.y*p2.y);
		d = (float) Math.sqrt(d);
		
		if(p1.x <p2.x)d = -1*d;
		
		return d;
	}
	
	public void initData(){
		for(int i = 0; i < data.size();i++){
			
			//add view in MapData
			final KfMapNodeView child = new KfMapNodeView(getContext());
			child.setText(data.get(i).getData());
			child.setVisibility(View.INVISIBLE);
			child.setPadding(30,30,30,30);
			data.get(i).setView(child);
			
			
			//set pre
			for(KfMapData d:data.get(i).getNex()){
				d.getPre().add(data.get(i));
			}
			
			//note as first node
			if(i == 0) child.setIsFirstView(true);
		
			addView(child,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			
			//set child listener
			child.setOnLongClickListener(new OnLongClickListener(){

					@Override
					public boolean onLongClick(View p1)
					{
						child.touchStatus = KfMapNodeView.TOUCH_STATUS_ON_TOUCH_DRAGING;
						
						
						return false;
					}
				});
				
			child.setOnTouchListener(new OnTouchListener(){

				float x,y;
					@Override
					public boolean onTouch(View p1, MotionEvent p2)
					{
						switch(p2.getAction()){
							case  MotionEvent.ACTION_UP:
								child.touchStatus = KfMapNodeView.TOUCH_STATUS_DEFAULT;
								break;
							case MotionEvent.ACTION_MOVE:
								if(child.touchStatus == KfMapNodeView.TOUCH_STATUS_ON_TOUCH_DRAGING);
								{
									if(x != p2.getRawX()){
										child.setRCenter(new PointF(child.getRCenter().x +p2.getRawX()-x,child.getRCenter().y));
									}
									if(y != p2.getRawY()){
										child.setRCenter(new PointF(child.getRCenter().x,child.getRCenter().y+p2.getRawY()-y));
									}
									isTooClose(child,true,false,300,1000);
									layout(child);
									x = p2.getRawX();
									y = p2.getRawY();
							}
								break;
							case MotionEvent.ACTION_DOWN:
								x = p2.getRawX();
								y = p2.getRawY();
								break;
						}
						
						return false;
					}
				});
			
		}
	}
	
	

	

	private boolean isTooClose(KfMapNodeView view,boolean min,boolean max,int minDistance, int maxDistance){
		int dis2;
		int dX;
		int dY;

		for(KfMapNodeView targetView:dispatchedViews){
			dX = (int) Math.abs(targetView.getRCenter().x - view.getRCenter().x);
			dY = (int) Math.abs(targetView.getRCenter().y - view.getRCenter().y);
			dis2 = (dX * dX) + (dY * dY);
			dis2 = (int) Math.sqrt(dis2);



			if((dis2 <minDistance && min )
			   && (targetView != view))
			{
				int minF = minDistance/20;
				float x = targetView.getRCenter().x+(targetView.getRCenter().x - view.getRCenter().x)/minF;
				float y = targetView.getRCenter().y+(targetView.getRCenter().y - view.getRCenter().y)/minF;

				targetView.setRCenter(new PointF(x,y));


				layout(targetView);

				isTooClose(targetView,min,max,minDistance,maxDistance);

				return true;
			}
			if(dis2 >= maxDistance && max){
				int maxF = maxDistance/10;

				float x = targetView.getRCenter().x-(targetView.getRCenter().x - view.getRCenter().x)/maxF;
				float y = targetView.getRCenter().y-(targetView.getRCenter().y - view.getRCenter().y)/maxF;

				targetView.setRCenter(new PointF(x,y));

				layout(targetView);

				targetView.setState(KfMapNodeView.TOUCH_STATUS_ON_TOUCH_DRAGING);
				isTooClose(targetView,min,max,450,2000);

			} 

			

//			else if(dis2 < maxDistance/2 &max){
//				targetView.setState(KfMapNodeView.STATE_DEFAULT);
//			}

		}
		
		

		return false;
	}
	
	
	private void layout(KfMapNodeView child)
	{
		int x = (int) (child.getRCenter().x+baseX);
		int y = (int)(child.getRCenter().y+baseY);
		int r = (int)(child.getRadius());
		child.layout(x - r, y - r, x + r, y + r);
		//child.setText(x+","+y);
	}
	

	@Override
	protected void onLayout(boolean p1, int p2, int p3, int p4, int p5)
	{
		int i = 0;
		for(KfMapData mapData:data){
			KfMapNodeView view = mapData.getView();
			setChildRCenter(view);
			//view.setRCenter(new PointF((float)Math.random()*getMeasuredWidth(),(float)Math.random()*getMeasuredHeight()));
			layout(view);
		}
		
	}
	
	private void onLoadData(){
		initData();
		
		//set status;
		drawStatus = DrawStatus.draw;
	}
	

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		measureChildren(widthMeasureSpec,heightMeasureSpec);

		//Toast.makeText(context,"measure",Toast.LENGTH_SHORT).show();
	}
	
	
	
	public void startFirstView(final KfMapNodeView view,final Canvas canvas){
		AnimationSet aS = new AnimationSet(true);
		
		Animation tA = new TranslateAnimation(
			-view.getRCenter().x,
			0,
			-view.getRCenter().y,
			0);
		
		tA.setFillAfter(true);
		tA.setDuration(100);
		tA.setAnimationListener(new Animation.AnimationListener(){

				@Override
				public void onAnimationStart(Animation p1)
				{

					isLoaded = true;
				}

				@Override
				public void onAnimationEnd(Animation p1)
				{
					spreadKfMapNodeView(view,canvas);
				}

				@Override
				public void onAnimationRepeat(Animation p1)
				{
					// TODO: Implement this method
				}
			});
		aS.addAnimation(tA);
		view.startAnimation(aS);
	}
	
	public void startView(int duration,final KfMapNodeView startView,final KfMapNodeView targetView,final Canvas canvas){
		
			startView.setVisibility(View.VISIBLE);
			targetView.setVisibility(View.VISIBLE);
		//for(int i = 0 ; i < getChildCount();i++){
			AnimationSet aS = new AnimationSet(true);
			
			int dx;
			int dy;
			dx = (int) (startView.getRCenter().x- targetView.getRCenter().x);
			dy = (int) (startView.getRCenter().y- targetView.getRCenter().y);
			//Animation tA = new TranslateAnimation(-child.getRelativeX(),0,-child.getRelativeY(),0);
			Animation tA = new TranslateAnimation(dx,0,dy,0);
			
			tA.setFillAfter(true);
			tA.setDuration(duration);
			//tA.setStartOffset(i*100/getChildCount());
			tA.setAnimationListener(new Animation.AnimationListener(){

					@Override
					public void onAnimationStart(Animation p1)
					{
						// TODO: Implem
					}

					@Override
					public void onAnimationEnd(Animation p1)
					{
					//	drawLine(canvas,startView,targetView,mPaint);
						spreadKfMapNodeView(targetView,canvas);
						targetView.isOnCurrectPosition = true;
					}

					@Override
					public void onAnimationRepeat(Animation p1)
					{
						// TODO: Implement this method
					}
				});
			RotateAnimation rotateAnim = new RotateAnimation(
				0
				,720
				,Animation.RELATIVE_TO_SELF
				,0.5f
				,Animation.RELATIVE_TO_SELF
				,0.5f);

			rotateAnim.setDuration(duration);
			rotateAnim.setFillAfter(true);

			aS.addAnimation(rotateAnim);
			
			
			aS.addAnimation(tA);
			targetView.startAnimation(aS);
		//}
	}
	

	

	private void setChildRCenter(KfMapNodeView child)
	{
		do{
			
			float x = (float) (Math.random() *(getMeasuredWidth()*2-getMeasuredWidth()));
			float y = (float) (Math.random() * (getMeasuredHeight()*2-getMeasuredHeight()));
			child.setRCenter(new PointF(x,y));
			
		}while(isTooClose(child,true,false,300,1000));
		dispatchedViews.add(child);
	}

	
	public void spreadKfMapNodeView(KfMapNodeView view,Canvas canvas){
		isLoaded = true;
		//get kfmapdata from view;
		int pos = data.size()-1;
		for(;pos>-1;pos--){
			if(data.get(pos).getData().equals(view.getText().toString()))
				break;
		}
		if(pos == -1) return ;
		KfMapData<String> d = data.get(pos);
		
		view.hasSpreaded = true;
		
		//spread
		for(int j = 0; j < d.getNex().size();j++){
			KfMapNodeView view2 = findNodeViewByString(d.getNex().get(j).getData());
			
			if(!view2.hasSpreaded)
				startView(300,view,view2,canvas);
			
		}
	}
	
	public void setData(List<KfMapData<String>> data){
		this.data = data;
		onLoadData();
	}
	
	
	
	public class KfMapNodeView extends TextView
	{

		Paint mPaint;
		
		Paint firPaint;
		
		boolean shouldMove = false;
		
		boolean hasSpreaded = false;

		boolean isOnCurrectPosition = false;
		
		PointF sCenter;
		
		PointF rCenter;
		
		
		static final int TOUCH_STATUS_DEFAULT = 0;
		static final int TOUCH_STATUS_ON_TOUCH_MOVING = 1;
		static final int TOUCH_STATUS_ON_TOUCH_DRAGING = 2;
		
		int touchStatus = TOUCH_STATUS_DEFAULT;
		
		int backGroundColor;
		
		int firstColor;
		
		static final int DRAW_STATUS_RECT = 0;

		static final int DRAW_STATUS_DEFAULT = DRAW_STATUS_RECT;

		static final int DRAW_STATUS_CIRCLE = 1;
		
		int drawStatus = DRAW_STATUS_DEFAULT;		
		boolean isFirstView = false;
		
		public KfMapNodeView(){
			this(null);
		}

		public KfMapNodeView(Context context){
			this(context,null);
		}

		public KfMapNodeView(Context context, AttributeSet attrs){
			super(context,attrs);
			
			//initial text
			setGravity(Gravity.CENTER);
			setTextColor(Color.parseColor("#eeeeee"));
			backGroundColor = Color.parseColor("#bcd4ee");
			firstColor = Color.parseColor("#ee3333");
		}
		


		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
		{
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);

			mPaint = new Paint();
			mPaint.setColor(backGroundColor);
			mPaint.setStrokeWidth(33);
			mPaint.setAntiAlias(true);
			firPaint = new Paint();
			firPaint.setColor(firstColor);
			firPaint.setStrokeWidth(33);
			firPaint.setAntiAlias(true);
			mPaint.setAlpha(160);
		}

		@Override
		protected void onDraw(Canvas canvas)
		{
			
			// TODO: Implement this method
			canvas.save();
			if(drawStatus == DRAW_STATUS_CIRCLE){
				if(isFirstView)
					canvas.drawCircle(getSCenter().x,getSCenter().y,getRadius(),firPaint);
				canvas.drawCircle(getSCenter().x,getSCenter().y,getRadius()*0.9f,mPaint);
			} else if (drawStatus == DRAW_STATUS_RECT){
				float l = 0;
				float t = 0;
				float r = l + getMeasuredWidth();
				float b = t + getMeasuredHeight();
				RectF rect = new RectF(l,t,r,b);
				if(isFirstView){
					float dx = getMeasuredWidth()*0.1f;
					float dy = getMeasuredHeight()*0.1f;
					
					canvas.drawRoundRect(rect,30,30,firPaint);
					canvas.drawRoundRect(l+dx,t+dy,r-dx,b-dy,25,25,mPaint);
				}else{
					
					canvas.drawRoundRect(rect,30,30,mPaint);
				}
				
			}	
			canvas.restore();

			super.onDraw(canvas);

		}

		public void setIsFirstView(boolean isFirstView)
		{
			this.isFirstView = isFirstView;
		}

		public boolean isFirstView()
		{
			return isFirstView;
		}

		public void setDrawStatus(int drawStatus)
		{
			this.drawStatus = drawStatus;
		}

		public int getDrawStatus()
		{
			return drawStatus;
		}

		
		public PointF getSCenter()
		{
			return new PointF(getMeasuredWidth()/2,getMeasuredHeight()/2);
		}

		
		public PointF getRCenter()
		{
			return rCenter;
		}
		
		public void setRCenter(PointF p){
			this.rCenter = p;
		}

		public void setState(int state)
		{
			this.touchStatus = state;
		}

		public int getState()
		{
			return touchStatus;
		}

		public float getRadius(){
			return Math.max(getMeasuredWidth(),getMeasuredHeight())/2;
		}
		

	}
	
	public class Circle{
		PointF circleCenter;
		float radius;

		public Circle(PointF circleCenter, float radius)
		{
			this.circleCenter = circleCenter;
			this.radius = radius;
		}

		public void setCircleCenter(PointF circleCenter)
		{
			this.circleCenter = circleCenter;
		}

		public PointF getCircleCenter()
		{
			return circleCenter;
		}

		public void setRadius(float radius)
		{
			this.radius = radius;
		}

		public float getRadius()
		{
			return radius;
		}
		
	}
	
	public class Line{
		PointF p1;
		PointF p2;
		
		KfMapNodeView view1;
		KfMapNodeView view2;
		
		//define a null k(float)
		static final float K_NULL = 10000f;

		public Line(KfMapNodeView view1, KfMapNodeView view2)
		{
			this.view1 = view1;
			this.view2 = view2;
		} 

		
		public void setView1(KfMapNodeView view1)
		{
			this.view1 = view1;
		}

		public KfMapNodeView getView1()
		{
			return view1;
		}

		public void setView2(KfMapNodeView view2)
		{
			this.view2 = view2;
		}

		public KfMapNodeView getView2()
		{
			return view2;
		}

		public void setP1(PointF p1)
		{
			view1.setRCenter(p1);
		}

		public PointF getP1()
		{
			return view1.getRCenter();
		}

		public void setP2(PointF p2)
		{
			view2.setRCenter(p2);
		}

		public PointF getP2()
		{
			return view2.getRCenter();
		}

		
		

		public float getK()
		{
			
			if(getP1().x == getP2().x)return K_NULL;
			else
			return (getP1().y-getP2().y)/(getP1().x-getP2().x);
		}
		
	}
	
}
