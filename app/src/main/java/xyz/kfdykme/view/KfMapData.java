package xyz.kfdykme.view;

import java.util.*;

public class KfMapData<T>
{
	T data;
	List<KfMapData<T>> pre = new ArrayList<KfMapData<T>>();
	List<KfMapData<T>> nex = new ArrayList<KfMapData<T>>();

	KfMapView.KfMapNodeView view;
	
	public KfMapData(T data)
	{
		this.data = data;
	}

	public void setView(KfMapView.KfMapNodeView view)
	{
		this.view = view;
	}

	public KfMapView.KfMapNodeView getView()
	{
		return view;
	}


	public void setData(T data)
	{
		this.data = data;
	}

	public T getData()
	{
		return data;
	}

	public void setPre(List<KfMapData<T>> pre)
	{
		this.pre = pre;
	}

	public List<KfMapData<T>> getPre()
	{
		return pre;
	}

	public void setNex(List<KfMapData<T>> nex)
	{
		this.nex = nex;
	}

	public List<KfMapData<T>> getNex()
	{
		return nex;
	}
	
	
}
