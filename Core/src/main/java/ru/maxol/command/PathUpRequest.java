package ru.maxol.command;

public class PathUpRequest extends Command{
    @Override
    public CommandType getType() {
        return CommandType.PATH_UP_REQUEST;
    }
}
