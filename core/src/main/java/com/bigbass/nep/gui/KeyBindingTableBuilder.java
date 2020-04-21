package com.bigbass.nep.gui;

import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.bigbass.nep.gui.actors.ContainerLabel;
import com.bigbass.nep.util.KeyBinding;


public class KeyBindingTableBuilder {
    private final Color ITEM_COLOR = new Color(0xA8C2D7FF);

    private final TextureRegion DOWN_ARROW_TEXTURE;
    private final TextureRegion UP_ARROW_TEXTURE;

    private Stage stage;
    private Table root;
    private float tableWidth;

    public Table Column;
    public ContainerLabel nodeViewText;

    private KeyBindingManager bindingManager;

    public KeyBindingTableBuilder(Stage stage, Table root, KeyBindingManager bindingManager, float tableWidth){
        this.stage = stage;
        this.root = root;
        this.tableWidth = tableWidth;
        this.bindingManager = bindingManager;

        DOWN_ARROW_TEXTURE = new TextureRegion(new Texture(Gdx.files.internal("textures/downArrow.png")));
        UP_ARROW_TEXTURE = new TextureRegion(new Texture(Gdx.files.internal("textures/upArrow.png")));
    }

    public void build(){
        final boolean wasVisible = root.isVisible();

        root.reset();
//        root.debugAll();

        root.setVisible(wasVisible);

        root.setWidth(tableWidth);

        Column = new Table(root.getSkin());
        Column.setWidth(tableWidth);

        ListHeader(Column);
        ListHotKeys(Column);

        root.add(Column).align(Align.top);

        root.align(Align.top);

        reposition();
    }

    public void reposition(){
        root.setPosition(Gdx.graphics.getWidth() - root.getWidth(), Gdx.graphics.getHeight());
    }

    private void ListHeader(Table root){
        root.row();
        final Skin rootSkin = root.getSkin();
        Table nested = new Table(rootSkin);

        ContainerLabel title = new ContainerLabel(new Skin());
        title.label.setText("Hot Key List");
        title.label.setAlignment(Align.center);
        title.setBackgroundColor(Color.GOLD);
        title.setForegroundColor(Color.GOLD);
        title.minWidth(root.getWidth());

        nested.align(Align.top);
        nested.add(title);

        root.add(nested);
    }

    private void ListHotKeys(Table root){
        root.row();
        final Skin rootSkin = root.getSkin();
        Table nested = new Table(rootSkin);
        int comboWidth = 200;
        for (Map.Entry<String, KeyBinding> binding : this.bindingManager.entrySet()) {
            ContainerLabel title = new ContainerLabel(new Skin());
            title.label.setText(binding.getValue().getHelp());
            title.label.setAlignment(Align.left);
            title.setBackgroundColor(ITEM_COLOR);
            title.setForegroundColor(ITEM_COLOR);
            title.minWidth(root.getWidth() - comboWidth);
            nested.add(title);

            ContainerLabel combo = new ContainerLabel(new Skin());
            combo.label.setText(binding.getValue().toString());
            combo.label.setAlignment(Align.right);
            combo.setBackgroundColor(ITEM_COLOR);
            combo.setForegroundColor(ITEM_COLOR);
            combo.minWidth(comboWidth);
            nested.add(combo);

            nested.row();
        }
        root.add(nested);
    }
}
