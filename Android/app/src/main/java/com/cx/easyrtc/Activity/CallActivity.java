package com.cx.easyrtc.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.cx.easyrtc.Agent.Agent;
import com.cx.easyrtc.Agent.AgentListAdapter;
import com.cx.easyrtc.EasyRTCApplication;
import com.cx.easyrtc.R;
import com.cx.easyrtc.Socket.SocketWraper;

import java.util.ArrayList;

public class CallActivity extends AppCompatActivity implements SocketWraper.SocketDelegate{

    private final String TAG = CallActivity.class.getName();

    private ListView mAgentsView;

    private AgentListAdapter mAgentListAdapter;

    private AlertDialog mAlertDialog;

    private ImageButton mCallButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        Log.e("skruazzz", "CallActivity onCreate");

        setUI();
        setAgents();
        setSocket();
        requestUserList();
    }

    @Override
    protected void onStop() {
        Log.e("skruazzz", "CallActivity onStop");
        SocketWraper.shareContext().removeListener(this);

        super.onStop();
    }

    private void setSocket() {
        SocketWraper.shareContext().addListener(this);
    }

    private void requestUserList() {
        SocketWraper.shareContext().requestUserList();
    }

    private void setUI() {
        mAgentsView = findViewById(R.id.AgentsView);
        setAlertDialog();
        setButton();
    }

    private void setAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CallActivity.this);
        builder.setTitle("收到呼叫");
        builder.setPositiveButton("接听", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.e("sliver", "CallActivity accept invite");
                jumpToNextActivity("recv");
            }
        });
        builder.setNegativeButton("挂断", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.e("sliver", "CallActivity refuse invite");
                SocketWraper.shareContext().ack(false);
            }
        });
        mAlertDialog = builder.create();
    }

    private void setButton() {
        mCallButton = findViewById(R.id.CallButton);
        mCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("sliver", "CallActivity calll button clicked");
                Agent agent = mAgentListAdapter.getChooseAgent();
                if (agent != null) {
                    SocketWraper.shareContext().setTarget(agent.id());
                    SocketWraper.shareContext().invite();
                }
            }
        });
    }

    private void setAgents() {
        mAgentListAdapter = new AgentListAdapter(this);
        mAgentsView.setAdapter(mAgentListAdapter);
    }

    private void jumpToNextActivity(String status) {
        Intent intent = new Intent(CallActivity.this, RTCActivity.class);
        intent.putExtra("status", status);

        Agent agent = mAgentListAdapter.getChooseAgent();
        if (agent.type().equals("Android_Camera")) {
            intent.putExtra("type", "camera");
        } else {
            intent.putExtra("type", "client");
        }

        startActivity(intent);
    }

    private void processSignal(String source, String target, String type, String value) {
        if (target.equals(SocketWraper.shareContext().getSource())) {
            if (type.equals("invite")) {
                SocketWraper.shareContext().setTarget(source);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAlertDialog.show();
                    }
                });
            }
            if (type.equals("ack")) {
                if(value.equals("yes")) {
                    jumpToNextActivity("send");
                }
            }
        } else {
            Log.e("sliver", "CallActivity get error target");
        }
    }

    @Override
    public void onUserAgentsUpdate(ArrayList<Agent> agents) {
        Log.e("sliver", "CallActivity onUserAgentsUpdate");
        mAgentListAdapter.reset();
        for (int i = 0; i < agents.size(); i++) {
            mAgentListAdapter.addAgent(agents.get(i));
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAgentListAdapter.update();
            }
        });
    }

    @Override
    public void onDisConnect() {
        Log.e("sliver", "CallActivity onDisConnect");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(EasyRTCApplication.getContext(), "can't connect to server", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRemoteEventMsg(String source, String target, String type, String value) {
        processSignal(source, target, type, value);
    }

    @Override
    public void onRemoteCandidate(int label, String mid, String candidate) {

    }
}