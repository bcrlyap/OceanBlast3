/**
* GameScene_base.java
* Mar 3, 2013
* 7:16:19 AM
* 
* @author B. Carla Yap
* @email bcarlayap@ymail.com
**/


package school.project.oceanblast3.scenes;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.DelayModifier;
import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.modifier.MoveXModifier;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.ButtonSprite.OnClickListener;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.util.modifier.IModifier;

import android.opengl.GLES20;
import android.util.Log;

import school.project.oceanblast3.ConstantsList;
import school.project.oceanblast3.ConstantsList.SceneType;
import school.project.oceanblast3.managers.SceneManager;
import school.project.oceanblast3.objects.Enemy;

public class GameScene extends BaseScene implements IOnSceneTouchListener{

	private int mScore=0;
	private Sprite userPlayer;
	private ButtonSprite pauseButton;
	private Enemy goldfish;
	private LinkedList TargetsToBeAdded;
	private LinkedList targetLL; 
	private LinkedList projectileLL;
	private LinkedList projectilesToBeAdded;
	private Text scoreText;
	private Text pelletSupplyText;
	private Font mFont;
	private PhysicsHandler physicsHandler;
	private BaseScene mScene;
	private int loopEnemies=0;
	
	
	
	@Override
	public void createScene() {
		this.mScene = this;	
		TargetsToBeAdded = new LinkedList();
		targetLL = new LinkedList();
		projectilesToBeAdded = new LinkedList();
		projectileLL = new LinkedList();
		createBackground();
		createGameObjects();
		createButtons();
		resourcesManager.game_analogControl.setPlayerPhysicsHandler(userPlayer, physicsHandler);
		resourcesManager.game_analogControl.setAnalogControl();
		

			setOnSceneTouchListener(this);
	 }

	@Override
	public void onBackKeyPressed() {
		SceneManager.getInstance().loadMenuScene();
		
	}

	@Override
	public SceneType getSceneType() {
		// TODO Auto-generated method stub
		return ConstantsList.SceneType.GAME;
	}

	@Override
	public void disposeScene() {
		
	}
	
	private void createBackground(){
	
		//background
		final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(0.0f, new Sprite(0, ConstantsList.CAMERA_HEIGHT - 
				 resourcesManager.game_parallaxLayerBackRegion.getHeight(),resourcesManager.game_parallaxLayerBackRegion,
				 vboManager)));
		 
		 autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-10.0f, new Sprite(0, ConstantsList.CAMERA_HEIGHT - 
				 resourcesManager.game_parallaxLayerFrontRegion.getHeight(), resourcesManager.game_parallaxLayerFrontRegion,
				 vboManager)));
		 this.setBackground(autoParallaxBackground);
			 
	}
	

	private void createGameObjects(){	
		
		final int centerX= (int) (ConstantsList.CAMERA_WIDTH - resourcesManager.game_playerRegion.getHeight())/2;	
		final int centerY=(int) (ConstantsList.CAMERA_HEIGHT - resourcesManager.game_playerRegion.getHeight())/2;	
	
		//player
		 final Sprite userPlayer = new Sprite(centerX, centerY, resourcesManager.game_playerRegion,vboManager);
		 this.physicsHandler = new PhysicsHandler(userPlayer);
		 userPlayer.registerUpdateHandler(physicsHandler);
		 this.attachChild(userPlayer);
		 this.userPlayer = userPlayer; 
		 
		 createSpriteSpawnTimeHandler();
		 
		 IUpdateHandler detect = new IUpdateHandler() {
			    public void reset() {
			    }

			    public void onUpdate(float pSecondsElapsed) {

			        Iterator<Sprite> targets = targetLL.iterator();
			        Sprite _target;
			        boolean hit = false;

			        while (targets.hasNext()) {
			            _target = targets.next();

			            if (_target.getX() <= -_target.getWidth()) {
			                removeSprite(_target, targets);
			                break;
			            }
			            Iterator<Sprite> projectiles = projectileLL.iterator();
			            Sprite _projectile;
			            while (projectiles.hasNext()) {
			                _projectile = projectiles.next();

			                if (_projectile.getX() >= camera.getWidth()
			                    || _projectile.getY() >= camera.getHeight()
			                    + _projectile.getHeight()
			                    || _projectile.getY() <= -_projectile.getHeight()) {
			                        removeSprite(_projectile, projectiles);
			                        continue;
			                }

			                if (_target.collidesWith(_projectile)) {
			                    removeSprite(_projectile, projectiles);
			                    hit = true;
			                    scoreText.setText("Score: " + (++mScore));
			                    break;
			                }
			                
			                if(_target.collidesWith(userPlayer)){
			                	hit = true;
			                	removeSprite(userPlayer);
			                	SceneManager.getInstance().setCurrentScene(SceneType.PAUSE);
			                }
			            }

			            if (hit) {
			                removeSprite(_target, targets);
			                
			                hit = false;
			            }

			        }
			        projectileLL.addAll(projectilesToBeAdded);
			        projectilesToBeAdded.clear();

			        targetLL.addAll(TargetsToBeAdded);
			        TargetsToBeAdded.clear();
			    }
			};



		 
		 		  
		this.scoreText = new Text(5, 5, resourcesManager.font, "Score: 0", "Score: XXXX".length(), vboManager);
		
		this.scoreText.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		this.scoreText.setAlpha(0.5f);
		this.attachChild(this.scoreText);
		
		this.registerUpdateHandler(detect);
	}
	
	private void createButtons(){
		 //listener for the pause button
		 OnClickListener pauseListener = new OnClickListener(){
			
				public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX,
						float pTouchAreaLocalY) {
					// call the setCurrentScene for pause button
					SceneManager.getInstance().setCurrentScene(SceneType.PAUSE);
				}
			};	
			
		 //pause button
		 pauseButton = new ButtonSprite(740,10,resourcesManager.game_btnPauseRegion,vboManager,pauseListener);
		 this.registerTouchArea(pauseButton);
		 this.setTouchAreaBindingOnActionDownEnabled(true);
		 this.attachChild(pauseButton);
		}
	
	
	private void createSpriteSpawnTimeHandler() {
	    TimerHandler spriteTimerHandler;
	    float mEffectSpawnDelay = 3f;

	    spriteTimerHandler = new TimerHandler(mEffectSpawnDelay, true,
	    new ITimerCallback() {

	        public void onTimePassed(TimerHandler pTimerHandler) {
	            addTarget();
	        }
	    });

	    engine.registerUpdateHandler(spriteTimerHandler);
	}
	
	public void addTarget() {
	    Random rand = new Random();

	    int x = (int) camera.getWidth() + (int) resourcesManager.game_enemyGoldfishRegion.getWidth();
	    int minY =(int) resourcesManager.game_enemyGoldfishRegion.getHeight();
	    int maxY = (int) (camera.getHeight() -(int) resourcesManager.game_enemyGoldfishRegion.getHeight());
	    int rangeY = maxY - minY;
	    int y = rand.nextInt(rangeY) + minY;

	    AnimatedSprite target = new AnimatedSprite(x, y, resourcesManager.game_enemyGoldfishRegion.deepCopy(), vboManager);
	   target.animate(200);
	    this.attachChild(target);

	    int minDuration = 2;
	    int maxDuration = 4;
	    int rangeDuration = maxDuration - minDuration;
	    int actualDuration = rand.nextInt(rangeDuration) + minDuration;

	    MoveXModifier mod = new MoveXModifier(actualDuration, target.getX(),
	        -target.getWidth());
	    target.registerEntityModifier(mod.deepCopy());

	    TargetsToBeAdded.add(target);

	}
	
	private void removeSprite(final Sprite _sprite, Iterator it) {
	    activity.runOnUpdateThread(new Runnable() {

	        public void run() {
	            mScene.detachChild(_sprite);
	        }
	    });
	    it.remove();
	}
	
	private void removeSprite(final Sprite _sprite) {
	    activity.runOnUpdateThread(new Runnable() {

	         public void run() {
	            mScene.detachChild(_sprite);
	        }
	    });
	}
	
	private void shootProjectile(final float pX, final float pY) {

		
		int offX = (int) (pX - userPlayer.getX());
	    int offY = (int) (pY - userPlayer.getY());
	    if (offX <= 0)
	        return;

	    final Sprite projectile;
	    projectile = new Sprite(userPlayer.getX()+100, userPlayer.getY()+50,
	    resourcesManager.game_pelletRegion.deepCopy(), vboManager);
	    this.attachChild(projectile);

	    int realX = (int) (camera.getWidth() + projectile.getWidth() / 2.0f);
	    float ratio = (float) offY / (float) offX;
	    int realY = (int) ((realX * ratio) + projectile.getY());

	    int offRealX = (int) (realX - projectile.getX());
	    int offRealY = (int) (realY - projectile.getY());
	    float length = (float) Math.sqrt((offRealX * offRealX)
	        + (offRealY * offRealY));
	    float velocity = 480.0f / 1.0f; // 480 pixels / 1 sec
	    float realMoveDuration = length / velocity;

	    MoveModifier mod = new MoveModifier(realMoveDuration,
	    projectile.getX(), realX, projectile.getY(), realY);
	    projectile.registerEntityModifier(mod.deepCopy());

	    projectilesToBeAdded.add(projectile);
	}

	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {

	    if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
	        final float touchX = pSceneTouchEvent.getX();
	        final float touchY = pSceneTouchEvent.getY();
	        shootProjectile(touchX, touchY);
	        return true;
	    }
	    return false;
	}


}
