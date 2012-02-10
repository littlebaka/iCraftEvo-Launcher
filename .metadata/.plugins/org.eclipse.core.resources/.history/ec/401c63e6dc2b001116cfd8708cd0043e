package com.kokakiwi.mclauncher.utils;

public enum State
{
    INIT(1), DETERMINING_PACKAGE(2), CHECKING_CACHE(3), DOWNLOADING(4), EXTRACTING_PACKAGES(
            5), UPDATING_CLASSPATH(6), SWITCHING_APPLET(7), INITIALIZE_REAL_APPLET(
            8), START_REAL_APPLET(9), DONE(10);
    
    private int    opcode;
    private String description;
    
    State()
    {
        this(State.values().length + 1);
    }
    
    State(String description)
    {
        this(State.values().length + 1, description);
    }
    
    State(int opcode)
    {
        this(opcode, null);
    }
    
    State(int opcode, String description)
    {
        this.opcode = opcode;
        this.description = description;
    }
    
    public int getOpCode()
    {
        return opcode;
    }
    
    public String getDescription()
    {
        return description;
    }
}
