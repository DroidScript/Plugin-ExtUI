app.CreateExtUI = function( options )
{ 
    return new ExtUI( options );
}

//Plugin wrapper class.
function ExtUI( options )
{
    this.plg = _CreatePlugin( "ru.starport.plugins.user.ExtUI", options );

	this.GetVersion = function( num, txt ) {

		return parseFloat( this.plg.Send( "GetVersion" ) );
	}

	// return list of class objTypeName methods
	this.GetMethods = function( classSimpleName ) {

		return this.plg.Send( "GetMethods", classSimpleName );
	}


	// handle events
	this.SetOnEvent = function( obj, objTypeName, callback ){

		this.plg.SendObj( "SetOnEvent", obj, objTypeName, callback.name );
	}

	// creating object
	this.CreateExtObject = function( classSimpleName ){

		var ret = this.plg.CreateObj( "ExtObject", classSimpleName );

	    if( ret ){

        var _myObj = new Obj(ret);

			return _myObj;

 		} else {

 		 	return null;
        }
	}

	//  execute method
	this.RunMethod = function( obj, methodName, paramsTypes, par1, par2, par3, par4 ){
		this.plg.SendObj( "RunMethod", obj, methodName, paramsTypes, par1, par2, par3, par4 );
		var ret = this.plg.Send("GetResult");
		return ret;
	}

	this.SetParamObject = function( obj, no ){

        if(obj!=null){
            this.plg.SendObj( "SetParamObject", obj, no);
        }
	}
}
