/*
	Created by Alexandr Strashko

	This plugin extends the native capabilities UI DroidScript.
	It allows work with API Android: to create new objects and to call
	new methods for them or existing DS objects.

	DroidScript Plugin class.
	(This is where you put your plugin code)
*/

package ru.starport.plugins.user;

import android.os.*;
import android.content.*;
import android.util.*;
import android.graphics.*;
import java.lang.reflect.*;
import java.io.*;
import android.app.*;
import android.view.*;
import android.graphics.drawable.*;
import java.util.*;
import java.lang.*;
import android.widget.*;
import android.text.*;
import android.view.ViewGroup.*;
import android.widget.DatePicker.*;

public class ExtUI
{
	public static String TAG = "ExtUI";
	public static float VERSION = 1.01f;
	private Method m_callscript;
	private Object m_parent;
	private Context m_ctx;

	private String methodResult = "";
	private String objClassName = "";
	private Object[] objParam = {null,null,null,null};

	// Contruct plugin.
	public ExtUI()
	{
		Log.d( TAG, "Creating plugin object");
	}

	// Initialise plugin.
	public void Init( Context ctx, Object parent )
	{
		try {
			Log.d( TAG, "Initialising plugin object");
			m_ctx = ctx;
			m_parent = parent;
			m_callscript = parent.getClass().getMethod( "CallScript", Bundle.class );

			// Your initialisation code goes here.
			//...
		}
		catch (Exception e) {
			Log.e( TAG, "Failed to Initialise plugin!", e );
		}
	}

	// Release plugin resources.
	public void Release()
	{
		// Your tidy up code goes here.
		//...
	}

	// Use this method to call a function in the user's script.
	private void CallScript( Bundle b )
	{
		try {
			m_callscript.invoke( m_parent, b );
		}
		catch (Exception e) {
			Log.e( TAG, "Failed to call script function!", e );
		}
	}

	// Handle commands from DroidScript
	public String CallPlugin( Bundle b, Object obj )
	{
		// Extract command
		String cmd = b.getString( "cmd" );

		// Choose command
		String ret = null;
		try {

			if( cmd.equals("GetVersion") ){

				return GetVersion( b );

			}else if( cmd.equals( "GetResult" ) ){

				return methodResult;

			} else if( cmd.equals( "RunMethod" ) ){

				methodResult = "";
				methodResult = RunMethod( b, obj );
				//methodResult = objClassName;

			}
			else if( cmd.equals( "SetOnEvent" ) ){

				SetOnEvent( b, obj );

			} else if( cmd.equals( "GetMethods" ) ){

				return getMethods( b );

			} else if( cmd.equals( "SetParamObject" ) ){

				setParamObject( b, obj );
			}
		}
		catch (Exception e) {

			Log.e( TAG, "Plugin command failed!", e);
		}
		return ret;
	}

	// Handle the CreateObject from DroidScript
	public Object CreateObject( Bundle b )
	{
		String type = b.getString("type");
		Object ret = null;

		try {
			if( type.equals("ExtObject") ){

				return CreateExtObject( b );

			}
		}
		catch (Exception e) {

			Log.e( TAG, "Plugin command failed!", e);
		}
		return ret;
	}
	private void clearParamsObjects(){
		for( byte _i = 0; _i < objParam.length;_i++ ) {

			objParam[_i] = null;
		}
	}

	private void setParamObject(Bundle b, Object obj )
	{
		Log.d( TAG, "Set object" );
		int _index = (int) Math.floor(Float.valueOf((Float) b.getFloat("p1")));
		clearParamsObjects();
		if(( _index >=0 ) && (_index <=4)) {
			objParam[_index] = obj;
		}
	}

	// Handle the GetVersion command.
	private String GetVersion( Bundle b )
	{
		Log.d( TAG, "Got GetVersion" );
		return Float.toString( VERSION );
	}

	// Handle events
	private void SetOnEvent( Bundle b, Object obj )
	{
		String _eventName = b.getString("p1");
		final String _command = b.getString("p2");
		switch( _eventName){
			case "SetOnTouch":{
				// for object allowing touch

				final Button _btn = (Button)obj;
				_btn.setOnClickListener( new View.OnClickListener(){

					//@Override
					public void onClick( View v){

						if( _command!="" ){
							Bundle b = new Bundle();
							b.putString( "cmd", _command );
							CallScript( b );
						}
					}
				});

				break;
			}

			case "SetOnDateChanged":{
				// for DatePicker

				final DatePicker _obj = (DatePicker)obj;

				_obj.init(_obj.getYear(),_obj.getMonth() ,_obj.getDayOfMonth(), new OnDateChangedListener(){

					//@Override
					public void onDateChanged( DatePicker view, int year, int monthOfYear, int dayOfYear){
						if( _command!=null ){
							Bundle b = new Bundle();
							b.putString( "cmd", _command );
							b.putInt("p1", view.getYear() );
							b.putInt("p2", view.getMonth() );
							b.putInt("p3", view.getDayOfMonth() );
							CallScript( b );
						}
					}
				});
				break;
			}
		}
	}

	// Handle RunMethod command
	private String RunMethod(Bundle b, Object obj ) {

		Object _result = null;
		String _methodName = b.getString("p1");
		String _paramsTypes = b.getString("p2");
		Object _p1 = b.get("p3");
		Object _p2 = b.get("p4");
		Object _p3 = b.get("p5");
		Object _p4 = b.get("p6");

		_result = ExecMethod(
				obj,
				_methodName,
				_paramsTypes,
				_p1,
				_p2,
				_p3,
				_p4
		);

		return (String)_result;
	}

	// Dinamic creation of object
	private Object CreateExtObject( Bundle b ){

		String _className =  b.getString( "p1" );

		Object _obj = null;
		View _view = null;

		if(_className.equals("AlertDialog")){

			AlertDialog.Builder builder = new AlertDialog.Builder(m_ctx);
			AlertDialog _dialog = builder.create();
			return _dialog;

		} else if(_className.equals("Toast")){

			Toast _toast = Toast.makeText(m_ctx, null, Toast.LENGTH_LONG);
			return _toast;
		}

		else {

			try {
				Class _class = Class.forName("android.widget." + _className);
				Constructor _con = _class.getConstructors()[0];
				try {

					_obj = _con.newInstance(m_ctx);

				} catch (InvocationTargetException e) {
					methodResult = "error: " + e;
				} catch (java.lang.InstantiationException e) {
					methodResult = "error: " + e;
				} catch (java.lang.IllegalAccessException e) {
					methodResult = "error: " + e;
				} catch (java.lang.IllegalArgumentException e) {
					methodResult = "error: " + e;
				}
			} catch (java.lang.ClassNotFoundException e) {
				methodResult = "error: " + e;
			}
		}

		try {
			_view = (View) _obj;
			_view.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		} catch(Exception e){}

		return _view;
	}

	// Matching arguments and parameters
	private Boolean getEqualArgsParamsCount(Class[] p_argsTypes, String[] p_paramsTypes) {

		Byte _i = 0;
		Byte _countMatches = 0;

		for (Class argType : p_argsTypes) {

			if( argType.getSimpleName().compareTo(p_paramsTypes[_i]) == 0) {

				_countMatches++;
			}
			_i++;
		}

		if (p_argsTypes.length == _countMatches ) {

			Log.i(TAG, "find method with passes args");
			return true;

		} else {

			return false;
		}

	}

	// Find and execute method
	// It is disabled reserved for future use
	private String ExecMethod( Object p_obj, String p_methodName, String p_paramsTypes, Object... p_params ){

		Object _result = "method id done";

		Class _cls;
		//Button _btn = (Button)p_obj;
		String _info ="";

		try {

			_cls = p_obj.getClass();

			Log.i(TAG, "class was found");
			_info += "2";

			Method[] methods = _cls.getMethods();
			ArrayList<Method> _findMethods = new ArrayList();

			Boolean _noParams = true;
			String[] _paramsTypes = new String[0];

			byte _paramsCount = 0;
			for (Object _i : p_params) {
				if (_i != null) {
					_paramsCount++;
				}
			}
			_info += "3";

			if (p_paramsTypes.length() > 0) {

				_noParams = false;
				_paramsTypes = p_paramsTypes.split(",");

				if (_paramsTypes.length != _paramsCount) {

					return "error: number of specified types and submitted parameters do not match " +
							"types - " + _paramsTypes.length + ", parameters - " + _paramsCount;
				}
			}

			Class[] _argsTypes = new Class[0];

			for (Method method : methods) {

				if (method.getName().equals(p_methodName)) {
					// method finded
					_info += "4";
					_findMethods.add(method);
				}
			}

			if (_findMethods.size() == 0) {
				_info += "5";
				return "error: method " + p_methodName + " not find";
			}

			Boolean _methodFinded = false;

			if (_findMethods.size() == 1) {
				_info += "6";
				Log.i(TAG, "find one method");
				_argsTypes = _findMethods.get(0).getParameterTypes();

				if (_argsTypes.length == 0) {
					_info += "7";
					_methodFinded = true;
				}

				if (_argsTypes.length == _paramsCount) {
					_info += "8";
					_methodFinded = getEqualArgsParamsCount(_argsTypes, _paramsTypes);
				}

			} else if (_findMethods.size() > 1) {
				_info += "9";
				Log.i(TAG, "find several methods");
				for (Method method : _findMethods) {

					_argsTypes = method.getParameterTypes();
					if (_argsTypes.length == 0) {

						_methodFinded = true;
						break;
					}
					if (_argsTypes.length == _paramsCount) {

						_methodFinded = getEqualArgsParamsCount(_argsTypes, _paramsTypes);
					}
					if (_methodFinded) {
						break;
					}

				}
			}
			_info += "!";

			if (_methodFinded) {

				Log.i(TAG, "execution method");

				try {

					Method m = _cls.getMethod(p_methodName, _argsTypes);

					for( int _i = 0; _i < _argsTypes.length;_i++) {
						if (_paramsTypes[_i].equals("int")) {

							p_params[_i] = (int) Math.floor(Float.valueOf((Float) p_params[_i]));
						}
					}

					for( byte _i = 0; _i < objParam.length;_i++ ) {
						if(objParam[_i] != null) {
							p_params[_i] = objParam[_i];
						}
					}

					// selects and run method depending on the number of parameters
					switch(_argsTypes.length){
						case 0:_result = m.invoke(p_obj);break;
						case 1:	_result = m.invoke(p_obj, p_params[0]);	break;
						case 2:_result = m.invoke(p_obj, p_params[0], p_params[1]);break;
						case 3:_result = m.invoke(p_obj, p_params[0], p_params[1], p_params[2]);break;
						case 4:_result = m.invoke(p_obj, p_params[0], p_params[1], p_params[2], p_params[3]);break;
					}

				} catch (NoSuchMethodException e) {
					_result = "error: " + e;

				} catch (InvocationTargetException e) {
					_result = "error: " + e;

				} catch (IllegalAccessException e) {
					_result = "error: " + e;

				} catch (Exception e) {
					_result = "error: " + e;

				}
			}
		}
		catch(Exception e){ _info+=e.toString();

		}
		//_btn.setText(_info);
		//_btn.setText(_result.toString());
		if( _result!=null ) {
			return _result.toString();
		} else {
			return "null";
		}
	}

	// return list of methods for specified className
	private String getMethods ( Bundle b) throws ClassNotFoundException {

		String _className = "android.widget." + b.getString( "p1" );
		ArrayList<String> _paramsList = new ArrayList<String>();
		ArrayList<String> _methodsList = new ArrayList<String>();
		StringBuilder _method = new StringBuilder();

		Class c = Class.forName( _className );
		Method[] methods = c.getMethods();
		for (Method method : methods) {

			Class[] paramTypes = method.getParameterTypes();
			_paramsList.clear();
			for (Class paramType : paramTypes) {

				_paramsList.add(paramType.getSimpleName());
			}
			_method.append( method.getName());
			_method.append( "(" );
			_method.append( _paramsList.toString() );
			_method.append( "):" );
			_method.append( method.getReturnType().getName() );
			_method.append( "|" );
		}
		return _method.toString();
	}

}


