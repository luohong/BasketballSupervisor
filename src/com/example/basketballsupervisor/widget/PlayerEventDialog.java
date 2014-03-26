package com.example.basketballsupervisor.widget;

import android.content.Context;
import android.widget.ViewFlipper;

import com.android.framework.core.widget.BaseDialog;
import com.example.basketballsupervisor.R;

public class PlayerEventDialog extends BaseDialog {

	protected ViewFlipper vFlipper;

	public PlayerEventDialog(Context context) {
		super(context);
	}

	@Override
	protected int getLayoutId() {
		return R.layout.dialog_player_event;
	}

	@Override
	protected void initDialogViews() {
		vFlipper = (ViewFlipper) findViewById(R.id.vf_events);

	}

	@Override
	protected void afterDialogViews() {

	}

	protected void showPrevious() {
		vFlipper.setInAnimation(getContext(), R.anim.left_in);
		vFlipper.setOutAnimation(getContext(), R.anim.right_out);
		vFlipper.showPrevious();
	}

	protected void showNext() {
		vFlipper.setInAnimation(getContext(), R.anim.right_in);
		vFlipper.setOutAnimation(getContext(), R.anim.left_out);
		vFlipper.showNext();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		int displayChild = vFlipper.getDisplayedChild();
		if (displayChild == 0) {
			dismiss();
		} else if (displayChild == 1) {
			showPrevious();
		}
	}
}
