package data.missions.da_itanoCircus;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;
import data.scripts.util.MagicRender;
import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.input.Keyboard;

public class MissionDefinition implements MissionDefinitionPlugin {

    private final String FILE = "diableavionics_highscore";
    private Integer highscore = -1;
    private static boolean freeCam=false, easyMode=false, music=true;

    @Override
    public void defineMission(MissionDefinitionAPI api) {

        if(highscore<0){
            String input=null;
            try{
                input = Global.getSettings().readTextFileFromCommon(FILE);
            } catch (IOException ex){
            }
            if(input!=null && !input.isEmpty()){
                highscore = Integer.parseInt(input);
            } else {
                highscore = 0;
            }
        }

        if(Keyboard.isKeyDown(Keyboard.KEY_E)){
            easyMode=!easyMode;
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_C)){
            freeCam=!freeCam;
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_M)){
            music=!music;
        }

        // Set up the fleets so we can add ships and fighter wings to them.
        // In this scenario, the fleets are attacking each other, but
        // in other scenarios, a fleet may be defending or trying to escape
        api.initFleet(FleetSide.PLAYER, "USN", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "ITN", FleetGoal.ATTACK, true);

        // Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "Wanzer pilot candidates");
        api.setFleetTagline(FleetSide.ENEMY, "VIRTUAL ENTITIES");

        // These show up as items in the bulleted list under
        // "Tactical Objectives" on the mission detail screen

        api.addBriefingItem("HIGHSCORE: "+highscore);
        if(freeCam){
            api.addBriefingItem("Free cam: ENABLED");
        } else {
            api.addBriefingItem("Free cam: DISABLED");
        }
        if(easyMode){
            api.addBriefingItem("Easy mode: ENABLED");
        } else {
            api.addBriefingItem("Easy mode: DISABLED");
        }
        if(music){
            api.addBriefingItem("Music: ENABLED");
        } else {
            api.addBriefingItem("Music: DISABLED");
        }


        // Set up the player's fleet.  Variant names come from the
        // files in data/variants and data/variants/fighters
        api.addToFleet(FleetSide.PLAYER, "diableavionics_valiantTrainer", FleetMemberType.SHIP, true);


        // Set up the enemy fleet.
        api.addToFleet(FleetSide.ENEMY, "buffalo_d_Standard", FleetMemberType.SHIP, false);

        // Set up the map.
        api.initMap(-ARENA_SIZE, ARENA_SIZE, -ARENA_SIZE, ARENA_SIZE);

        api.addPlugin(new Plugin());
    }


    private final int ARENA_SIZE=1000;

    private float timer = 0;
    private boolean runOnce=false;
    ShipAPI player=null, enemy=null;

    public class Plugin extends BaseEveryFrameCombatPlugin {

        ////////////////////////////////////
        //                                //
        //      BATTLE INITIALISATION     //
        //                                //
        ////////////////////////////////////

        @Override
        public void init(CombatEngineAPI engine) {
            timer=0;
            clock=0; digitA=0; digitB=0; digitC=0;
            float screenX=Global.getSettings().getScreenWidth();
            float screenY=Global.getSettings().getScreenHeight();

            CLOCK_POS = new Vector2f(0,screenY/2-60);

            if(!freeCam){
                //fixed cam
                engine.getViewport().setExternalControl(true);
                float ratio = screenX/screenY;
                //find low left corner
                float lly=-ARENA_SIZE/2-40;
                float llx=lly*ratio;
                engine.getViewport().set(llx, lly,(ARENA_SIZE*2+80)*ratio, ARENA_SIZE*2+80);
                engine.getViewport().setCenter(new Vector2f());
            }
        }

        @Override
        public void advance(float amount, List<InputEventAPI> events) {
            CombatEngineAPI engine = Global.getCombatEngine();

            engine.getCombatUI().hideShipInfo();

            //apply starting bonus to the enemy missiles (might be removed for custom missiles later on)
            if(!runOnce){
                boolean checkEnemy=false, checkPlayer=false;

                if(enemy==null && !engine.getFleetManager(FleetSide.ENEMY).getDeployedCopy().isEmpty()){
                    enemy = engine.getFleetManager(FleetSide.ENEMY).getShipFor(engine.getFleetManager(FleetSide.ENEMY).getDeployedCopy().get(0));
                    if(!easyMode){
                        enemy.getMutableStats().getMissileWeaponRangeBonus().modifyMult("itano", 1.5f);
                        enemy.getMutableStats().getMissileHealthBonus().modifyMult("itano", 0.5f);
                    } else {
                        enemy.getMutableStats().getMissileHealthBonus().modifyMult("itano", 0.1f);
                    }
                    checkEnemy=true;
                }

                if(player==null && !engine.getFleetManager(FleetSide.PLAYER).getDeployedCopy().isEmpty()){
                    player = engine.getFleetManager(FleetSide.PLAYER).getShipFor(engine.getFleetManager(FleetSide.PLAYER).getDeployedCopy().get(0));
                    Vector2f location = player.getLocation();
                    location.x=0;
                    location.y=-512;
                    if(easyMode){
                        player.getMutableStats().getTimeMult().modifyMult("itano", 1.25f);
                        player.getMutableStats().getEngineDamageTakenMult().modifyMult("itano", 0.1f);
                    } else {
                        player.getMutableStats().getEngineDamageTakenMult().modifyMult("itano", 0.5f);
                    }
                    player.setCollisionClass(CollisionClass.FIGHTER);
                    checkPlayer=true;
                }

                runOnce=(checkEnemy && checkPlayer);
            }

            if(player==null || enemy==null )return;
            if(engine.isPaused())engine.setPaused(false);

            engine.setDoNotEndCombat(true);

            //keep enemy ship away
            Vector2f location = enemy.getLocation();
            location.scale(0);
            Vector2f.add(location, new Vector2f(0,4000), location);

            //collision box
            collisionBox(player, amount);

            //timer
            if(player.isAlive()){
                timer+=amount;

                //stages
                if(timer>120 && stage<3){
                    //transition trigger
                    stage=3;
                } else if(timer>60 && stage<2){
                    //transition trigger
                    stage=2;
                    if(!easyMode){
                        enemy.getMutableStats().getMissileWeaponRangeBonus().modifyMult("itano", 3f);
                    }
                }

                //missiles
                missile(engine, stage, timer, player);

                //clock
                clock(timer);

                //failed attempts at fixing the jumping trail
                //player.getEngineController().setFlameLevel(player.getEngineController().getShipEngines().get(0).getEngineSlot(), 1);
                //player.getEngineController().forceShowAccelerating();

            } else {
                score();
                engine.setDoNotEndCombat(false);
            }

            //music
            if(music){
                MissileAPI m = AIUtils.getNearestEnemyMissile(player);
                float distance = -1;
                if(m!=null){
                    distance = MathUtils.getDistanceSquared(
                            player.getLocation(),
                            m.getLocation()
                    );
                }
                music(
                        timer,
                        player.isAlive(),
                        distance
                );
            }
        }
    }

    //MISSILES
    private int timer_stage1=0, timer_stage2=0, timer_stage3=0;

    private void missile(CombatEngineAPI engine, Integer stage, float time, ShipAPI player){

        //stage 1 simple Pilum
        if(timer>timer_stage1+1){
            timer_stage1=(int)timer;
            Vector2f point = MathUtils.getPoint(new Vector2f(), 1500, time*30);
            CombatEntityAPI missile = engine.spawnProjectile(
                    enemy,
                    enemy.getAllWeapons().get(0),
                    //"harpoon",
                    "pilum",
                    point,
                    VectorUtils.getFacing(point)+180,
                    new Vector2f()
            );
        }

        //stage 2 devious Pilum
        if(stage>1 && timer>timer_stage2+2.5f){
            timer_stage2=(int)timer;
            Vector2f point = MathUtils.getPoint(new Vector2f(), 1500, time*30);
            CombatEntityAPI missile = engine.spawnProjectile(
                    enemy,
                    enemy.getAllWeapons().get(0),
                    "harpoon",
                    //"pilum",
                    point,
                    VectorUtils.getFacing(point)+180,
                    new Vector2f()
            );
        }

        //stage 3 infuriating Mines
        if(stage>2 && timer>timer_stage3+5){
            timer_stage3=(int)timer;
            float angle=VectorUtils.getFacing(player.getVelocity());
            Vector2f point = MathUtils.getRandomPointInCone(player.getLocation(), 500, angle-30,angle+30);
            CombatEntityAPI missile = engine.spawnProjectile(
                    enemy,
                    enemy.getAllWeapons().get(0),
                    //"diableavionics_thrush",
                    //"pilum",
                    "minelayer1",
                    point,
                    MathUtils.getRandomNumberInRange(0, 360),
                    new Vector2f()
            );
        }
    }

    //MUSIC MANAGER

    private final Map<Integer, String> backer = new HashMap<>();
    {
        backer.put(1, "itano_high");
        backer.put(0, "itano_early");
    }
    private final Map<Integer, String> drum = new HashMap<>();
    {
        drum.put(1, "itano_highDrum");
        drum.put(0, "itano_earlyDrum");
    }
    private final float LOOP = 15.238f;
    private int musicTrack=-1;
    private boolean zinger=false;

    private void music(float time, boolean alive, float danger){

        //DEATH
        if(!alive){
            if(!zinger){
                zinger=true;
                //ZINGER
                Global.getSoundPlayer().playUISound("itano_death", 1, 1);
            }
            return;
        }

        //ALIVE
        if(time>LOOP*8){
            //after all the early levels, switch to the final track loop
            if(musicTrack!=1){
                Global.getSoundPlayer().playUILoop(backer.get(musicTrack), 1, 0.01f);
                Global.getSoundPlayer().playUILoop(drum.get(musicTrack), 1, 0.01f);
                musicTrack=1;
            }
        } else if(musicTrack!=0){
            musicTrack=0;
        }

        Global.getSoundPlayer().playUILoop(backer.get(musicTrack), 1, 1);

        //variable drums depending on the danger proximity
        if(alive){
            float level=0.30f;
            if(danger>0){
                level = 0.3f+(float)Math.sin(Math.min(MathUtils.FPI/2,5000/danger));
            }
            Global.getSoundPlayer().playUILoop(drum.get(musicTrack), 1, level);
        }
    }

    //CLOCK DISPLAY

    private int stage = 1;
    private int clock=0, digitA=0, digitB=0, digitC=0;
    private boolean elite=false;
    private final Vector2f DIGIT_SIZE = new Vector2f(22.5f,22.5f);
    private Vector2f CLOCK_POS = new Vector2f();
    private final Map<Integer, SpriteAPI> digit1 = new HashMap<>();
    {
        digit1.put(0, Global.getSettings().getSprite("fx","itano_0"));
        digit1.put(1, Global.getSettings().getSprite("fx","itano_1"));
        digit1.put(2, Global.getSettings().getSprite("fx","itano_2"));
        digit1.put(3, Global.getSettings().getSprite("fx","itano_3"));
        digit1.put(4, Global.getSettings().getSprite("fx","itano_4"));
        digit1.put(5, Global.getSettings().getSprite("fx","itano_5"));
        digit1.put(6, Global.getSettings().getSprite("fx","itano_6"));
        digit1.put(7, Global.getSettings().getSprite("fx","itano_7"));
        digit1.put(8, Global.getSettings().getSprite("fx","itano_8"));
        digit1.put(9, Global.getSettings().getSprite("fx","itano_9"));
    }
    private final Map<Integer, SpriteAPI> digit2 = new HashMap<>();
    {
        digit2.put(0, Global.getSettings().getSprite("fx","itano_0"));
        digit2.put(1, Global.getSettings().getSprite("fx","itano_1"));
        digit2.put(2, Global.getSettings().getSprite("fx","itano_2"));
        digit2.put(3, Global.getSettings().getSprite("fx","itano_3"));
        digit2.put(4, Global.getSettings().getSprite("fx","itano_4"));
        digit2.put(5, Global.getSettings().getSprite("fx","itano_5"));
        digit2.put(6, Global.getSettings().getSprite("fx","itano_6"));
        digit2.put(7, Global.getSettings().getSprite("fx","itano_7"));
        digit2.put(8, Global.getSettings().getSprite("fx","itano_8"));
        digit2.put(9, Global.getSettings().getSprite("fx","itano_9"));
    }
    private final Map<Integer, SpriteAPI> digit3 = new HashMap<>();
    {
        digit3.put(0, Global.getSettings().getSprite("fx","itano_0"));
        digit3.put(1, Global.getSettings().getSprite("fx","itano_1"));
        digit3.put(2, Global.getSettings().getSprite("fx","itano_2"));
        digit3.put(3, Global.getSettings().getSprite("fx","itano_3"));
        digit3.put(4, Global.getSettings().getSprite("fx","itano_4"));
        digit3.put(5, Global.getSettings().getSprite("fx","itano_5"));
        digit3.put(6, Global.getSettings().getSprite("fx","itano_6"));
        digit3.put(7, Global.getSettings().getSprite("fx","itano_7"));
        digit3.put(8, Global.getSettings().getSprite("fx","itano_8"));
        digit3.put(9, Global.getSettings().getSprite("fx","itano_9"));
    }
    private void clock (float time){
        if((int)time>clock){
            clock++;

            if(!elite){
                //regular clock
                digitA++;
                if(digitA>9){
                    digitA=0;
                    digitB++;
                    if(digitB>9){
                        digitB=0;
                        digitC++;
                        if(digitC>9){
                            elite=true;
                            digitA=9;
                            digitB=9;
                            digitC=9;
                        }
                    }
                }

                //visual
                Vector2f size = new Vector2f(DIGIT_SIZE);
                size.scale(1f+stage);
                MagicRender.screenspace(
                        digit1.get(digitA),
                        MagicRender.positioning.CENTER,
                        new Vector2f(CLOCK_POS.x+size.x*1.05f, CLOCK_POS.y), new Vector2f(),
                        size, new Vector2f(),
                        0, 0,
                        Color.white, false,
                        0,0,0,0,0,
                        0, 1f, 0.1f,
                        CombatEngineLayers.CLOUD_LAYER
                );
                MagicRender.screenspace(
                        digit2.get(digitB),
                        MagicRender.positioning.CENTER,
                        CLOCK_POS, new Vector2f(),
                        size, new Vector2f(),
                        0, 0,
                        Color.white, false,
                        0,0,0,0,0,
                        0, 1f, 0.1f,
                        CombatEngineLayers.CLOUD_LAYER
                );
                MagicRender.screenspace(
                        digit3.get(digitC),
                        MagicRender.positioning.CENTER,
                        new Vector2f(CLOCK_POS.x-size.x*1.05f, CLOCK_POS.y), new Vector2f(),
                        size, new Vector2f(),
                        0, 0,
                        Color.white, false,
                        0,0,0,0,0,
                        0, 1f, 0.1f,
                        CombatEngineLayers.CLOUD_LAYER
                );
            } else {
                //player beat 999s AMAZING!
                //visual
                Vector2f size = new Vector2f(DIGIT_SIZE);
                size.scale(1f+stage);
                MagicRender.screenspace(
                        digit1.get(digitA),
                        MagicRender.positioning.CENTER,
                        new Vector2f(CLOCK_POS.x+size.x*1.05f, CLOCK_POS.y), new Vector2f(),
                        size, new Vector2f(),
                        0, 0,
                        Color.red, false,
                        4,2,0.5f,1,0.05f,
                        0, 1f, 0.1f,
                        CombatEngineLayers.CLOUD_LAYER
                );
                MagicRender.screenspace(
                        digit2.get(digitB),
                        MagicRender.positioning.CENTER,
                        CLOCK_POS, new Vector2f(),
                        size, new Vector2f(),
                        0, 0,
                        Color.red, false,
                        4,2,0.5f,1,0.05f,
                        0, 1f, 0.1f,
                        CombatEngineLayers.CLOUD_LAYER
                );
                MagicRender.screenspace(
                        digit3.get(digitC),
                        MagicRender.positioning.CENTER,
                        new Vector2f(CLOCK_POS.x-size.x*1.05f, CLOCK_POS.y), new Vector2f(),
                        size, new Vector2f(),
                        0, 0,
                        Color.red, false,
                        4,2,0.5f,1,0.05f,
                        0, 1f, 0.1f,
                        CombatEngineLayers.CLOUD_LAYER
                );
            }
        }
    }

    //END SCREEN

    private final Vector2f TEXT1_SIZE = new Vector2f(450,90);
    private final Vector2f TEXT2_SIZE = new Vector2f(360,60);
    private final Vector2f SCORE_SIZE = new Vector2f(90,90);
    private final SpriteAPI SCORE = Global.getSettings().getSprite("fx","itano_score");
    private final SpriteAPI HIGHSCORE = Global.getSettings().getSprite("fx","itano_highscore");
    private boolean ended=false;
    private final Map<Integer, SpriteAPI> SCORE1 = new HashMap<>();
    {
        SCORE1.put(0, Global.getSettings().getSprite("fx","itano_0"));
        SCORE1.put(1, Global.getSettings().getSprite("fx","itano_1"));
        SCORE1.put(2, Global.getSettings().getSprite("fx","itano_2"));
        SCORE1.put(3, Global.getSettings().getSprite("fx","itano_3"));
        SCORE1.put(4, Global.getSettings().getSprite("fx","itano_4"));
        SCORE1.put(5, Global.getSettings().getSprite("fx","itano_5"));
        SCORE1.put(6, Global.getSettings().getSprite("fx","itano_6"));
        SCORE1.put(7, Global.getSettings().getSprite("fx","itano_7"));
        SCORE1.put(8, Global.getSettings().getSprite("fx","itano_8"));
        SCORE1.put(9, Global.getSettings().getSprite("fx","itano_9"));
    }
    private final Map<Integer, SpriteAPI> SCORE2 = new HashMap<>();
    {
        SCORE2.put(0, Global.getSettings().getSprite("fx","itano_0"));
        SCORE2.put(1, Global.getSettings().getSprite("fx","itano_1"));
        SCORE2.put(2, Global.getSettings().getSprite("fx","itano_2"));
        SCORE2.put(3, Global.getSettings().getSprite("fx","itano_3"));
        SCORE2.put(4, Global.getSettings().getSprite("fx","itano_4"));
        SCORE2.put(5, Global.getSettings().getSprite("fx","itano_5"));
        SCORE2.put(6, Global.getSettings().getSprite("fx","itano_6"));
        SCORE2.put(7, Global.getSettings().getSprite("fx","itano_7"));
        SCORE2.put(8, Global.getSettings().getSprite("fx","itano_8"));
        SCORE2.put(9, Global.getSettings().getSprite("fx","itano_9"));
    }
    private final Map<Integer, SpriteAPI> SCORE3 = new HashMap<>();
    {
        SCORE3.put(0, Global.getSettings().getSprite("fx","itano_0"));
        SCORE3.put(1, Global.getSettings().getSprite("fx","itano_1"));
        SCORE3.put(2, Global.getSettings().getSprite("fx","itano_2"));
        SCORE3.put(3, Global.getSettings().getSprite("fx","itano_3"));
        SCORE3.put(4, Global.getSettings().getSprite("fx","itano_4"));
        SCORE3.put(5, Global.getSettings().getSprite("fx","itano_5"));
        SCORE3.put(6, Global.getSettings().getSprite("fx","itano_6"));
        SCORE3.put(7, Global.getSettings().getSprite("fx","itano_7"));
        SCORE3.put(8, Global.getSettings().getSprite("fx","itano_8"));
        SCORE3.put(9, Global.getSettings().getSprite("fx","itano_9"));
    }

    private void score (){
        if(!ended){
            ended=true;

            //write highscore to file
            if(!easyMode && clock>highscore){
                //write highscore
                highscore=clock;
                try {
                    Global.getSettings().writeTextFileToCommon(FILE, highscore.toString());
                }
                catch (IOException ex) {
                    Global.getLogger(MissionDefinition.class).warn("Failed to write common file: " + ex);
                }
            }

            //SCORE

            MagicRender.screenspace(
                    SCORE,
                    MagicRender.positioning.CENTER,
                    new Vector2f(0, SCORE_SIZE.y*1.05f), new Vector2f(),
                    TEXT1_SIZE, new Vector2f(),
                    0, 0,
                    Color.white, false,
                    0,0,0,0,0,
                    0, 10f, 5f,
                    CombatEngineLayers.CLOUD_LAYER
            );
            if(!elite){
                MagicRender.screenspace(
                        digit1.get(digitA),
                        MagicRender.positioning.CENTER,
                        new Vector2f(SCORE_SIZE.x*1.05f, 0), new Vector2f(),
                        SCORE_SIZE, new Vector2f(),
                        0, 0,
                        Color.white, false,
                        0,0,0,0,0,
                        0, 10f, 5f,
                        CombatEngineLayers.CLOUD_LAYER
                );
                MagicRender.screenspace(
                        digit2.get(digitB),
                        MagicRender.positioning.CENTER,
                        new Vector2f(), new Vector2f(),
                        SCORE_SIZE, new Vector2f(),
                        0, 0,
                        Color.white, false,
                        0,0,0,0,0,
                        0, 10f, 5f,
                        CombatEngineLayers.CLOUD_LAYER
                );
                MagicRender.screenspace(
                        digit3.get(digitC),
                        MagicRender.positioning.CENTER,
                        new Vector2f(-SCORE_SIZE.x*1.05f, 0), new Vector2f(),
                        SCORE_SIZE, new Vector2f(),
                        0, 0,
                        Color.white, false,
                        0,0,0,0,0,
                        0, 10f, 5f,
                        CombatEngineLayers.CLOUD_LAYER
                );
            } else {
                //player beat 999s AMAZING!
                MagicRender.screenspace(
                        digit1.get(digitA),
                        MagicRender.positioning.CENTER,
                        new Vector2f(SCORE_SIZE.x*1.05f, 0), new Vector2f(),
                        SCORE_SIZE, new Vector2f(),
                        0, 0,
                        Color.red, false,
                        4,2,0.5f,1,0.05f,
                        0, 10f, 5f,
                        CombatEngineLayers.CLOUD_LAYER
                );
                MagicRender.screenspace(
                        digit2.get(digitB),
                        MagicRender.positioning.CENTER,
                        new Vector2f(), new Vector2f(),
                        SCORE_SIZE, new Vector2f(),
                        0, 0,
                        Color.red, false,
                        4,2,0.5f,1,0.05f,
                        0, 10f, 5f,
                        CombatEngineLayers.CLOUD_LAYER
                );
                MagicRender.screenspace(
                        digit3.get(digitC),
                        MagicRender.positioning.CENTER,
                        new Vector2f(-SCORE_SIZE.x*1.05f, 0), new Vector2f(),
                        SCORE_SIZE, new Vector2f(),
                        0, 0,
                        Color.red, false,
                        4,2,0.5f,1,0.05f,
                        0, 10f, 5f,
                        CombatEngineLayers.CLOUD_LAYER
                );
            }

            //HIGHSCORE

            MagicRender.screenspace(
                    HIGHSCORE,
                    MagicRender.positioning.CENTER,
                    new Vector2f(0, -SCORE_SIZE.y*1.05f), new Vector2f(),
                    TEXT2_SIZE, new Vector2f(),
                    0, 0,
                    Color.white, false,
                    0,0,0,0,0,
                    0, 10f, 5f,
                    CombatEngineLayers.CLOUD_LAYER
            );

            //parse score to digits
            int units=(highscore%10), tens=0, hundreds=0;

            if(highscore>=10){
                if(highscore<100){
                    tens = ((int)(highscore/10))%10;
                } else {
                    tens = ((int)(highscore/10))%10;
                    hundreds = ((int)(highscore/100))%10;
                }
            }

            if(highscore>clock){
                MagicRender.screenspace(
                        SCORE1.get(hundreds),
                        MagicRender.positioning.CENTER,
                        new Vector2f(-SCORE_SIZE.x*1.05f, -SCORE_SIZE.y*2.05f), new Vector2f(),
                        SCORE_SIZE, new Vector2f(),
                        0, 0,
                        Color.white, false,
                        0,0,0,0,0,
                        0, 10f, 5f,
                        CombatEngineLayers.CLOUD_LAYER
                );
                MagicRender.screenspace(
                        SCORE2.get(tens),
                        MagicRender.positioning.CENTER,
                        new Vector2f(0, -SCORE_SIZE.y*2.05f), new Vector2f(),
                        SCORE_SIZE, new Vector2f(),
                        0, 0,
                        Color.white, false,
                        0,0,0,0,0,
                        0, 10f, 5f,
                        CombatEngineLayers.CLOUD_LAYER
                );
                MagicRender.screenspace(
                        SCORE3.get(units),
                        MagicRender.positioning.CENTER,
                        new Vector2f(SCORE_SIZE.x*1.05f, -SCORE_SIZE.y*2.05f), new Vector2f(),
                        SCORE_SIZE, new Vector2f(),
                        0, 0,
                        Color.white, false,
                        0,0,0,0,0,
                        0, 10f, 5f,
                        CombatEngineLayers.CLOUD_LAYER
                );
            } else {
                //player beat highscore
                MagicRender.screenspace(
                        SCORE1.get(hundreds),
                        MagicRender.positioning.CENTER,
                        new Vector2f(-SCORE_SIZE.x*1.05f, -SCORE_SIZE.y*2.05f), new Vector2f(),
                        SCORE_SIZE, new Vector2f(),
                        0, 0,
                        Color.red, false,
                        4,2,0.5f,1,0.05f,
                        0, 10f, 5f,
                        CombatEngineLayers.CLOUD_LAYER
                );
                MagicRender.screenspace(
                        SCORE2.get(tens),
                        MagicRender.positioning.CENTER,
                        new Vector2f(0, -SCORE_SIZE.y*2.05f), new Vector2f(),
                        SCORE_SIZE, new Vector2f(),
                        0, 0,
                        Color.red, false,
                        4,2,0.5f,1,0.05f,
                        0, 10f, 5f,
                        CombatEngineLayers.CLOUD_LAYER
                );
                MagicRender.screenspace(
                        SCORE3.get(units),
                        MagicRender.positioning.CENTER,
                        new Vector2f(SCORE_SIZE.x*1.05f, -SCORE_SIZE.y*2.05f), new Vector2f(),
                        SCORE_SIZE, new Vector2f(),
                        0, 0,
                        Color.red, false,
                        4,2,0.5f,1,0.05f,
                        0, 10f, 5f,
                        CombatEngineLayers.CLOUD_LAYER
                );
            }
        }
    }

    //COLLISION BOX

    private final SpriteAPI BORDER_X = Global.getSettings().getSprite("fx","itano_border"), BORDER_Y = Global.getSettings().getSprite("fx","itano_border");
    private final Vector2f BORDER_SIZE = new Vector2f(512,64);
    private void collisionBox (ShipAPI ship, float amount){
        Vector2f loc = ship.getLocation();

        if(Math.abs(loc.x)>ARENA_SIZE*0.5f){

            //visual box
            float scale = Math.min(1,(Math.abs(loc.x)-ARENA_SIZE*0.5f)/(ARENA_SIZE*0.5f));
            Vector2f size = new Vector2f(BORDER_SIZE.x*(0.5f+0.5f*scale),BORDER_SIZE.y);
            Vector2f locBorder = new Vector2f(Math.copySign(ARENA_SIZE, loc.x), loc.y);
            MagicRender.singleframe(
                    BORDER_X,
                    locBorder,
                    size,
                    Math.copySign(90, loc.x),
                    new Color(1f,1f,1f,scale),
                    false
            );

            //bounce off bounds
            if(Math.abs(loc.x)>ARENA_SIZE){
                loc.x=Math.copySign(ARENA_SIZE, loc.x);
                ship.getVelocity().x*=-0.5f;
                ship.getVelocity().y*=0.25f;
            }
//            //stick to bounds
//            if(Math.abs(loc.x)>ARENA_SIZE*0.9f){
//                ship.getVelocity().x*=1-amount*10;
//                ship.getVelocity().y*=1-amount*5;
//            }
        }

        if(Math.abs(loc.y)>ARENA_SIZE*0.5f){

            //visual box
            float scale = Math.min(1,(Math.abs(loc.y)-ARENA_SIZE*0.5f)/(ARENA_SIZE*0.5f));
            Vector2f size = new Vector2f(BORDER_SIZE.x*(0.5f+0.5f*scale),BORDER_SIZE.y);
            Vector2f locBorder = new Vector2f(loc.x, Math.copySign(ARENA_SIZE, loc.y));
            MagicRender.singleframe(
                    BORDER_Y,
                    locBorder,
                    size,
                    90+Math.copySign(90, loc.y),
                    new Color(1f,1f,1f,scale),
                    false
            );

            //bounce off bounds
            if(Math.abs(loc.y)>ARENA_SIZE){
                loc.y=Math.copySign(ARENA_SIZE, loc.y);
                ship.getVelocity().y*=-0.25f;
                ship.getVelocity().x*=0.5f;
            }
//            //stick to bounds
//            if(Math.abs(loc.y)>ARENA_SIZE*0.9f){
//                ship.getVelocity().x*=1-amount*5;
//                ship.getVelocity().y*=1-amount*10;
//            }
        }
    }
}
