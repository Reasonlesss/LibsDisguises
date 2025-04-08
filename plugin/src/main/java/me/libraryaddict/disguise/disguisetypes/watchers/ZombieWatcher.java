package me.libraryaddict.disguise.disguisetypes.watchers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodDescription;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsRemovedIn;

public class ZombieWatcher extends InsentientWatcher {

    public ZombieWatcher(Disguise disguise) {
        super(disguise);
    }

    public boolean isAdult() {
        return !isBaby();
    }

    public boolean isBaby() {
        return getData(MetaIndex.ZOMBIE_BABY);
    }

    @MethodDescription("If the zombie is a baby")
    public void setBaby(boolean baby) {
        sendData(MetaIndex.ZOMBIE_BABY, baby);
    }

    public void setAdult() {
        setBaby(false);
    }

    public void setBaby() {
        setBaby(true);
    }

    @NmsAddedIn(NmsVersion.v1_13)
    public boolean isConverting() {
        return getData(MetaIndex.ZOMBIE_CONVERTING_DROWNED);
    }

    @NmsAddedIn(NmsVersion.v1_13)
    public void setConverting(boolean converting) {
        sendData(MetaIndex.ZOMBIE_CONVERTING_DROWNED, converting);
    }

    @Deprecated
    @NmsRemovedIn(NmsVersion.v1_14)
    public boolean isAggressive() {
        return getData(MetaIndex.ZOMBIE_AGGRESSIVE);
    }

    @Deprecated
    @NmsRemovedIn(NmsVersion.v1_14)
    public void setAggressive(boolean handsup) {
        sendData(MetaIndex.ZOMBIE_AGGRESSIVE, handsup);
    }
}
