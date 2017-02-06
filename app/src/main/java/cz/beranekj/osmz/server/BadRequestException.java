package cz.beranekj.osmz.server;

public class BadRequestException extends ServerException
{

    public BadRequestException()
    {
        super(400);
    }
}
