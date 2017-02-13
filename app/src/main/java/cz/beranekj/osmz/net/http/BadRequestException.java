package cz.beranekj.osmz.net.http;

public class BadRequestException extends ServerException
{

    public BadRequestException()
    {
        super(400);
    }
}
