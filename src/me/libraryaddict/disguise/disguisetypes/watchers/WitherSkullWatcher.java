package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;

public class WitherSkullWatcher extends FlagWatcher
{

    public WitherSkullWatcher(Disguise disguise)
    {
        super(disguise);
    }

    public boolean isBlue()
    {
        return (boolean) getData(FlagType.WITHER_SKULL_BLUE);
    }

    public void setBlue(boolean blue)
    {
        setData(FlagType.WITHER_SKULL_BLUE, blue);
        sendData(FlagType.WITHER_SKULL_BLUE);
    }

}
