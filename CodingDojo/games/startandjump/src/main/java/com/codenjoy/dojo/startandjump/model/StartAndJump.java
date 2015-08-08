package com.codenjoy.dojo.startandjump.model;

import com.codenjoy.dojo.startandjump.services.Events;
import com.codenjoy.dojo.startandjump.services.HeroStatus;
import com.codenjoy.dojo.services.*;

import java.util.LinkedList;
import java.util.List;

/**
 * О! Это самое сердце игры - борда, на которой все происходит.
 * Если какой-то из жителей борды вдруг захочет узнать что-то у нее, то лучше ему дать интефейс {@see Field}
 * Борда реализует интерфейс {@see Tickable} чтобы быть уведомленной о каждом тике игры. Обрати внимание на {StartAndJump#tick()}
 */
public class StartAndJump implements Tickable, Field {

    public static final int MAX_PLATFORM_LENGTH = 3;
    private final PlatformGenerator platformGenerator;
    private Level level;
    private List<Player> players;
    private List<Platform> platforms;
    private int tickCounter;

    private final int size;
    private List<Wall> walls;

    public StartAndJump(Dice dice, Level level) {
        this.level = level;
        size = level.getSize();
        players = new LinkedList<>();
        platformGenerator = new PlatformGenerator(dice, size, MAX_PLATFORM_LENGTH);
    }

    /**
     * @see Tickable#tick()
     */
    @Override
    public void tick() {
        tickCounter++;
        platforms.addAll(platformGenerator.generateRandomPlatforms());
//set player JUMPING status and/or jumpCounter since last IDLE
        for (Player player : players) {
            Hero hero = player.getHero();
            hero.tick();
        }
//move world
        for (Platform platform : platforms) {
            platform.tick();
        }
//remove platforms that out of the world
        for (Platform platform : platforms.toArray(new Platform[0])) {
            if (platform.isOutOf(size)) {
                platforms.remove(platform);
            }
        }
//moving hero and status changing
        for (Player player : players) {
            Hero hero = player.getHero();

        //moving hero
            if (hero.getStatus() == HeroStatus.FALLING) {
                //Very podozritelno
                if (!platforms.contains(PointImpl.pt(hero.getX() + 1, hero.getY() - 1))) {
                    hero.falls();
                }
            } else if (hero.getStatus() == HeroStatus.JUMPING) {
                hero.jumps();
            }
        //status changing
            boolean isPlatformUnderHero = platforms.contains(PointImpl.pt(hero.getX(), hero.getY() - 1));
            boolean isPlatformUnderHeroOnNextStep = platforms.contains(PointImpl.pt(hero.getX() + 1, hero.getY() - 1));
            if (isPlatformUnderHero || isPlatformUnderHeroOnNextStep) {
                hero.setStatus(HeroStatus.IDLE);
            } else {
                if(hero.getStatus() == HeroStatus.IDLE) {
                    hero.setAlreadyJumped(1);
                }
                hero.setStatus(HeroStatus.FALLING);
            }
        //kill hero in wall(spikes)
            if (walls.contains(hero)) {
                loseGame(player, hero);
            }
        }
        //kill hero inside platforms
        for (Player player : players) {
            Hero hero = player.getHero();
            if (platforms.contains(hero)) {
                loseGame(player, hero);
            }
        }
        for (Player player : players) {
            player.event(Events.STILL_ALIVE);
        }
    }

    private void loseGame(Player player, Hero hero) {
        player.event(Events.LOSE);
        platformGenerator.setPreviousY(2);
        hero.dies();
    }

    public int size() {
        return size;
    }

    public void newGame(Player player) {
        if (!players.contains(player)) {
            players.add(player);
        }
        player.newHero();

        walls = level.getWalls();
        platforms = level.getPlatforms();
    }

    public void remove(Player player) {
        players.remove(player);
    }

    public BoardReader reader() {
        return new BoardReader() {
            private int size = StartAndJump.this.size;

            @Override
            public int size() {
                return size;
            }

            @Override
            public Iterable<? extends Point> elements() {
                List<Point> result = new LinkedList<Point>();
                result.addAll(getHeroes());
                result.addAll(walls);
                result.addAll(platforms);
                return result;
            }
        };
    }

    public List<Hero> getHeroes() {
        List<Hero> heroes = new LinkedList<Hero>();
        for (Player player : players) {
            heroes.add(player.getHero());
        }
        return heroes;
    }

    List<Platform> getPlatforms() {
        return platforms;
    }

    public int getTickCounter() {
        return tickCounter;
    }
}