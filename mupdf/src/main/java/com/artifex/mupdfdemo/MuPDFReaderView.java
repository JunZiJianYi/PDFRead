package com.artifex.mupdfdemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MuPDFReaderView extends ReaderView {


	public enum Mode {Viewing, Selecting, Drawing}
	private final Context mContext;
	private boolean mLinksEnabled = false;
	private static boolean isLongClickModule = false;
	private Mode mMode = Mode.Viewing;
	private boolean tapDisabled = false;
	private int tapPageMargin;
	float  startX = 0;
	float  startY= 0;
	Timer timer = null;

	protected void onTapMainDocArea() {}
	protected void onDocMotion() {}
	protected void onHit(Hit item) {};

	public void setLinksEnabled(boolean b) {
		mLinksEnabled = b;
		resetupChildren();
	}

	public void setMode(Mode m) {
		mMode = m;
	}


	//图纸两边的留白为1/5
	private void setup()
	{
		// Get the screen size etc to customise tap margins.
		// We calculate the size of 1 inch of the screen for tapping.
		// On some devices the dpi values returned are wrong, so we
		// sanity check it: we first restrict it so that we are never
		// less than 100 pixels (the smallest Android device screen
		// dimension I've seen is 480 pixels or so). Then we check
		// to ensure we are never more than 1/5 of the screen width.
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);
		tapPageMargin = (int)dm.xdpi;
		if (tapPageMargin < 100)
			tapPageMargin = 100;
		if (tapPageMargin > dm.widthPixels/5)
			tapPageMargin = dm.widthPixels/5;
	}

	public MuPDFReaderView(Context context) {
		super(context);
		mContext = context;
		setup();
	}

	public MuPDFReaderView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mContext = context;
		setup();
	}


	//按下并抬起的手势事件
	public boolean onSingleTapUp(MotionEvent e) {
		LinkInfo link = null;

		if (mMode == Mode.Viewing && !tapDisabled) {
			MuPDFView pageView = (MuPDFView) getDisplayedView();
			Hit item = pageView.passClickEvent(e.getX(), e.getY());
			onHit(item);
			if (item == Hit.Nothing) {
				if (mLinksEnabled && pageView != null
				&& (link = pageView.hitLink(e.getX(), e.getY())) != null) {
					link.acceptVisitor(new LinkInfoVisitor() {
						@Override
						public void visitInternal(LinkInfoInternal li) {
							// Clicked on an internal (GoTo) link
							setDisplayedViewIndex(li.pageNumber);
						}

						@Override
						public void visitExternal(LinkInfoExternal li) {
							Intent intent = new Intent(Intent.ACTION_VIEW, Uri
									.parse(li.url));
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
							mContext.startActivity(intent);
						}

						@Override
						public void visitRemote(LinkInfoRemote li) {
							// Clicked on a remote (GoToR) link
						}
					});
				} else if (e.getX() < tapPageMargin) {
					super.smartMoveBackwards();
				} else if (e.getX() > super.getWidth() - tapPageMargin) {
					super.smartMoveForwards();
				} else if (e.getY() < tapPageMargin) {
					super.smartMoveBackwards();
				} else if (e.getY() > super.getHeight() - tapPageMargin) {
					super.smartMoveForwards();
				} else {
					onTapMainDocArea();
				}
			}
		}
		return super.onSingleTapUp(e);
	}

	@Override
	public boolean onDown(MotionEvent e) {

		return super.onDown(e);
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		MuPDFView pageView = (MuPDFView)getDisplayedView();
		switch (mMode) {
		case Viewing:
			if (!tapDisabled)
				onDocMotion();


			Log.e("测试", e1.toString()+"强"+e2.toString()+"强"+distanceX+"强"+distanceY);
			return super.onScroll(e1, e2, distanceX, distanceY);
		case Selecting:
			if (pageView != null)

				pageView.selectText(e1.getX(), e1.getY(), e2.getX(), e2.getY());
			return true;
		default:
			return true;
		}
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {

		Log.e("测试", "1");
		switch (mMode) {
		case Viewing:
			return super.onFling(e1, e2, velocityX, velocityY);
		default:
			return true;
		}
	}

	public boolean onScaleBegin(ScaleGestureDetector d) {
		// Disabled showing the buttons until next touch.
		// Not sure why this is needed, but without it
		// pinch zoom can make the buttons appear
		tapDisabled = true;
		return super.onScaleBegin(d);
	}

	public boolean onTouchEvent(MotionEvent event) {



		if ( mMode == Mode.Drawing )
		{
			float x = event.getX();
			float y = event.getY();
			switch (event.getAction())
			{
				case MotionEvent.ACTION_DOWN:
					touch_start(x, y);
					break;

			}
		}

		if ((event.getAction() & event.getActionMasked()) == MotionEvent.ACTION_DOWN)
		{
			tapDisabled = false;
		}

		return super.onTouchEvent(event);
	}

	private float mX, mY;

	private static final float TOUCH_TOLERANCE = 2;

	public void touch_start(float x, float y) {

		MuPDFView pageView = (MuPDFView)getDisplayedView();
		if (pageView != null)
		{
			pageView.startDraw(x, y);
		}
		mX = x;
		mY = y;
	}

	private void touch_move(float x, float y) {

		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE)
		{
			MuPDFView pageView = (MuPDFView)getDisplayedView();
			if (pageView != null)
			{
				pageView.continueDraw(x, y);
			}
			mX = x;
			mY = y;
		}
	}

	private void touch_up() {
		MuPDFView pageView = (MuPDFView)getDisplayedView();
		if (pageView != null)
		{
			pageView.saveDraw();
		}

	}

	protected void onChildSetup(int i, View v) {
		if (SearchTaskResult.get() != null
				&& SearchTaskResult.get().pageNumber == i)
			((MuPDFView) v).setSearchBoxes(SearchTaskResult.get().searchBoxes);
		else
			((MuPDFView) v).setSearchBoxes(null);

		((MuPDFView) v).setLinkHighlighting(mLinksEnabled);

		((MuPDFView) v).setChangeReporter(new Runnable() {
			public void run() {
				applyToChildren(new ViewMapper() {
					@Override
					void applyToView(View view) {
						((MuPDFView) view).update();
					}
				});
			}
		});
	}

	protected void onMoveToChild(int i) {
		if (SearchTaskResult.get() != null
				&& SearchTaskResult.get().pageNumber != i) {
			SearchTaskResult.set(null);
			resetupChildren();
		}
	}

	@Override
	protected void onMoveOffChild(int i) {
		View v = getView(i);
		if (v != null)
			((MuPDFView)v).deselectAnnotation();
	}

	protected void onSettle(View v) {
		// When the layout has settled ask the page to render
		// in HQ
		((MuPDFView) v).updateHq(false);
	}

	protected void onUnsettle(View v) {
		// When something changes making the previous settled view
		// no longer appropriate, tell the page to remove HQ
		((MuPDFView) v).removeHq();
	}

	@Override
	protected void onNotInUse(View v) {
		((MuPDFView) v).releaseResources();
	}

	@Override
	protected void onScaleChild(View v, Float scale) {
		((MuPDFView) v).setScale(scale);
	}

	//获取图纸坐标
	public  Map<String, String> getDrawingXY(View view,MotionEvent ev){
		Map<String, String> getDrawingXYMap =  new HashMap<>();

		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				startX = ev.getX();
				startY = ev.getY();
				timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						isLongClickModule = true;
					}
				}, 300);
				break;
			case MotionEvent.ACTION_MOVE:
				double deltaX = Math.sqrt((ev.getX() - startX) * (ev.getX() - startX) + (ev.getY() - startY) * (ev.getY() - startY));
				if (deltaX > 20 && timer != null) { // 移动大于20像素
					timer.cancel();
					timer = null;
				}
				if(isLongClickModule){

					float getFX = 1.0f * (super.scrollX + ev.getX() - 2*super.relativleLeft) / super.getDisplayedView().getWidth() * super.getWidth();
					float getFY = 1.0f * (super.scrollY + (view.getHeight() - ev.getY())) / super.getDisplayedView().getHeight() * super.getHeight();

					if(getFX >= 0 && getFX <= super.getWidth() ) {

						String getSX = String.valueOf(getFX);
						String getSY = String.valueOf(getFY);

						getDrawingXYMap.put("XValue", getSX);
						getDrawingXYMap.put("YValue", getSY);


					}
					timer = null;
				}
				break;
			default:
				isLongClickModule = false;
				if ( timer != null) {
					timer.cancel();
					timer = null;
				}
		}

		return getDrawingXYMap;
	}

	//坐标反向展示
	public Map<String, String> getXY(View view,float x, float y)
	{
		Map<String, String> getDrawingXYMap =  new HashMap<>();

		float drawingXInt =x / super.getWidth()* super.getDisplayedView().getWidth()  + 2*super.relativleLeft - super.scrollX ;

		float drawingYInt = view.getHeight() -  (y / super.getHeight() * super.getDisplayedView().getHeight()  - super.scrollY);


		String getSX = String.valueOf(drawingXInt);
		String getSY = String.valueOf(drawingYInt);

		getDrawingXYMap.put("XValue", getSX);
		getDrawingXYMap.put("YValue", getSY);

		return getDrawingXYMap;
	}

	//定位坐标
	public void coordinatesDraw(){



	}



}
