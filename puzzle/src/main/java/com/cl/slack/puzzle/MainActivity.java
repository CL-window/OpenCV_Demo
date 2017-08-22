package com.cl.slack.puzzle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.cl.slack.permission.PermissionsManager;
import com.cl.slack.permission.PermissionsResultAction;
import com.cl.slack.puzzle.puzzle.Puzzle15Activity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // TODO: 17/8/21 暂时只处理一次权限
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
            @Override
            public void onGranted() {

            }

            @Override
            public void onDenied(String permission) {
                Toast.makeText(MainActivity.this, getString(R.string.puzzle_need_permission) + permission, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onEasyClick(View view) {
        startPuzzleGame(0);
    }

    public void onMiddleClick(View view) {
        startPuzzleGame(1);
    }

    public void onHardyClick(View view) {
        startPuzzleGame(2);
    }

    private void startPuzzleGame(int d) {
        Intent intent = new Intent(this, Puzzle15Activity.class);
        switch (d) {
            case 0:
                intent.putExtra(Puzzle15Activity.KEY_DIFFICULTY, Puzzle15Activity.DIFFICULTY_EASY);
                break;
            case 2:
                intent.putExtra(Puzzle15Activity.KEY_DIFFICULTY, Puzzle15Activity.DIFFICULTY_HARDY);
                break;
            case 1:
            default:
                intent.putExtra(Puzzle15Activity.KEY_DIFFICULTY, Puzzle15Activity.DIFFICULTY_MIDDLE);
                break;
        }
        startActivity(intent);
    }
}
