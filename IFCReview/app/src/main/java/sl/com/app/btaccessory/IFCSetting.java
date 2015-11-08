package sl.com.app.btaccessory;

/**
 * Created by shenlong on 9/25/2015.
 */
public class IFCSetting {
    private String _name;
    private String _code;
    public IFCSetting(String name, String code)
    {
        _name = name;
        _code = code;
    }
    public String getName(){return _name;}
    public String getCode(){return _code;}
    public byte[] getData()
    {
        return IFCBusiness.getData(_code.toUpperCase());
    }
}
