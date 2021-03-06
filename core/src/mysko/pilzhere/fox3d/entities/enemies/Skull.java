package mysko.pilzhere.fox3d.entities.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Vector3;

import mysko.pilzhere.fox3d.constants.Constants;
import mysko.pilzhere.fox3d.entities.enemies.ai.SkullAI;
import mysko.pilzhere.fox3d.models.ModelInstanceBB;
import mysko.pilzhere.fox3d.rect.RectanglePlus;
import mysko.pilzhere.fox3d.rect.filters.RectanglePlusFilter;
import mysko.pilzhere.fox3d.screens.GameScreen;

public class Skull extends Enemy {
	private final SkullAI ai;
	private final TextureRegion currentTexReg;
	private final TextureRegion idleTexReg;
	private float damageTimer;

	public Skull(final Vector3 position, final GameScreen screen) {
		super(position, screen);
		this.position.add(Constants.HALF_UNIT, 0, Constants.HALF_UNIT);

		super.mdlInst = new ModelInstanceBB(screen.game.getCellBuilder().mdlEnemy);

		idleTexReg = new TextureRegion((Texture) screen.game.getAssMan().get(screen.game.getAssMan().enemies), 0, 0, 16,
				16);
		currentTexReg = idleTexReg;

		mdlInst.materials.get(0).set(TextureAttribute.createDiffuse(currentTexReg));
		mdlInst.materials.get(0).set(new ColorAttribute(ColorAttribute.Diffuse, Color.WHITE));
		mdlInst.materials.get(0).set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
		mdlInst.materials.get(0).set(new FloatAttribute(FloatAttribute.AlphaTest));

		final float rectWidth = Constants.HALF_UNIT;
		final float rectHeight = Constants.HALF_UNIT;
		rect = new RectanglePlus(this.position.x, this.position.z, rectWidth, rectHeight, id,
				RectanglePlusFilter.ENEMY);
		rect.setPosition(this.position.x - rect.getWidth() / 2, this.position.z - rect.getHeight() / 2);
		screen.game.getRectMan().addRect(rect);

		rect.oldPosition.set(rect.x, rect.y);
		rect.newPosition.set(rect.x, rect.y);

		ai = new SkullAI(this);
	}

	@Override
	public void destroy() {
		super.destroy(); // should be last.
	}

	private void flashRedIfHit(final float delta) {
		damageTimer = Math.max(damageTimer - delta * 5f, 0f);
		final ColorAttribute colorAttribute = (ColorAttribute) mdlInst.materials.get(0).get(ColorAttribute.Diffuse);
		colorAttribute.color.set(Color.WHITE.cpy().lerp(Color.RED, damageTimer));
	}

	@Override
	public void render3D(final ModelBatch mdlBatch, final Environment env, final float delta) {
		mdlInst.transform.setToLookAt(screen.getCurrentCam().direction.cpy().rotate(Vector3.Z, 180f), Vector3.Y);
		mdlInst.transform.setTranslation(position.cpy().add(0, Constants.HALF_UNIT, 0));

		super.render3D(mdlBatch, env, delta);
	}

	@Override
	public void subHp(final int amount) {
		super.subHp(amount);
		damageTimer = 1f;
	}

	@Override
	public void tick(final float delta) {
		ai.tick(delta);

		screen.checkOverlaps(rect, delta);

		position.set(rect.x + rect.getWidth() / 2, 0, rect.y + rect.getHeight() / 2);

		rect.oldPosition.set(rect.x, rect.y);

		flashRedIfHit(delta);
	}
}
