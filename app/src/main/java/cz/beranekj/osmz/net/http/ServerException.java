package cz.beranekj.osmz.net.http;

public class ServerException extends Exception
{
    private final int code;

    public ServerException()
    {
        this(500);
    }

    public ServerException(int code)
    {
        this.code = code;
    }
    public ServerException(int code, String message)
    {
        super(message);
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }
}
