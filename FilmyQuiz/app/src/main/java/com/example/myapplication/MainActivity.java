package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button mTrueButton;
    private Button mFalseButton;
    private Button mPreviousButton;
    private Button mNextButton;
    private TextView mQuestionTextView;
    private Question[] mQuestionBank = new Question[] {
        new Question(R.string.question_vijay,true),
        new Question(R.string.question_alluarjun,false),
        new Question(R.string.question_geethagovindam,true),
        new Question(R.string.question_rashmika,false),
        new Question(R.string.question_arjunreddy,false)
    };
    private int mCurrentIndex = 0;
    private int mIndexAdjustment = mQuestionBank.length;

    private void updateQuestion() {
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);
    }

    private void checkAnswer(boolean userPressedAnswer) {
        boolean isAnswerTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
        int messageResId = 0;

        if(userPressedAnswer == isAnswerTrue) {
            messageResId = R.string.correct_toast;
        } else {
            messageResId = R.string.incorrect_toast;
        }

        Toast.makeText(MainActivity.this,messageResId,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mQuestionTextView = (TextView) findViewById(R.id.question_text_view);

        mTrueButton = (Button) findViewById(R.id.true_button);

        mTrueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkAnswer(true);
            }
        });

        mFalseButton = (Button) findViewById(R.id.false_button);

        mFalseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                checkAnswer(false);
            }
        });

        mPreviousButton = (Button) findViewById(R.id.previous_button);

        mPreviousButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if (mCurrentIndex == 0) {
                    Toast.makeText(MainActivity.this, R.string.no_previous_question, Toast.LENGTH_SHORT).show();
                } else {
                    mCurrentIndex = (mCurrentIndex - 1) % mQuestionBank.length;
                    updateQuestion();
                }
            }
        });

        mNextButton = (Button) findViewById(R.id.next_button);

        mNextButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                updateQuestion();
            }
        });

        updateQuestion();
    }
}