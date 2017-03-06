package cz.beranekj.osmz2.net.http;

public class BadRequestException extends ServerException
{

    public BadRequestException()
    {
        super(400);
    }
}
