package io.github.putrasattvika.jrmcremote.ui.settings;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.putrasattvika.jrmcremote.R;
import io.github.putrasattvika.jrmcremote.RxBus;
import io.github.putrasattvika.jrmcremote.event.PlayListUpdatedEvent;
import io.github.putrasattvika.jrmcremote.event.ResetViewPageEvent;
import io.github.putrasattvika.jrmcremote.event.ServerConnectedEvent;
import io.github.putrasattvika.jrmcremote.ui.base.BaseFragment;
import io.github.putrasattvika.jrmcremote.utils.request.JRiverMCWSRequestUtil;

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com
 * Date: 9/1/16
 * Time: 9:59 PM
 * Desc: SettingsFragment
 */
public class SettingsFragment extends BaseFragment {
    @BindView(R.id.host_text_settings)
    EditText hostText;

    @BindView(R.id.port_text_settings)
    EditText portText;

    @BindView(R.id.connect_button_settings)
    Button connectButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @OnClick(R.id.connect_button_settings)
    public void connect(View view) {
        try {
            String host = hostText.getText().toString();
            int port = Integer.parseInt(portText.getText().toString());

            if (JRiverMCWSRequestUtil.getInstance().connect(host, port)) {
                InputMethodManager imm = (InputMethodManager)this.getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                RxBus.getInstance().post(new ServerConnectedEvent());
                RxBus.getInstance().post(new PlayListUpdatedEvent(null));
                RxBus.getInstance().post(new ResetViewPageEvent());
            }
        } catch (Exception ignore) {}
    }
}
