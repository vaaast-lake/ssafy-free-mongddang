/** @jsxImportSource @emotion/react */

import { Modal } from '@/shared/ui/Modal';
import {
  inputContainer,
  modalContainer,
  modalContent,
  modalTopBar,
} from './DietModal.styles';
import { TopBar } from '@/shared/ui/TopBar';
import DietModalBtnGroup from '../DietModalBtnGroup/DietModalBtnGroup';
import DietImage from '../DietImage/DietImage';
import { TextField } from '@/shared/ui/TextField';
import { Button } from '@/shared/ui/Button';
import { useCallback, useEffect, useState } from 'react';
import { debounce } from 'lodash';

type DietModalProps = {
  closeDietModal: () => void;
};

const DietModal = (props: DietModalProps) => {
  // 마지막 버튼 활성화
  const [isDisabled, setIsDisabled] = useState(true);
  const [diet, setDiet] = useState('');

  // 식단 텍스트 등록
  const debounceSaveDiet = useCallback(
    debounce((value: string) => {
      setDiet(value);
    }, 500),
    []
  );

  const handleInputDiet = (e: React.ChangeEvent<HTMLInputElement>) => {
    debounceSaveDiet(e.target.value);
  };

  // 식단 저장 버튼 활성화
  useEffect(() => {
    const handleDisabledBtn = () => {
      if (diet !== '') {
        setIsDisabled(false);
      }
    };
    return handleDisabledBtn();
  }, [diet]);

  return (
    <div>
      <Modal css={modalContainer}>
        {/* 탑 바 */}
        <TopBar
          type="modal"
          css={modalTopBar}
          iconHandler={() => {
            props.closeDietModal();
          }}
        >
          지금 뭐 먹어?
        </TopBar>

        <div css={modalContent}>
          {/* 시간 버튼 4종 */}
          <DietModalBtnGroup />

          {/* 식단 이미지 삽입 */}
          <DietImage />

          {/* 식단 텍스트 입력 */}
          <TextField
            color="dark"
            defaultValue=""
            label=""
            maxRows={10}
            multiLine
            placeholder="뭐 먹는지 적어줘! (✨사진, 글 둘 중 하나는 꼭 해줘:))"
            type="text"
            variant="outlined"
            css={inputContainer}
            value={diet}
            onChange={handleInputDiet}
          />

          {/* 저장 버튼 */}
          <Button
            color="primary"
            fontSize="1.25"
            fullwidth
            isShadow
            scale="A200"
            variant="contained"
            handler={() => {}}
            disabled={isDisabled}
          >
            저장하고 밥 먹기 시작
          </Button>
        </div>
      </Modal>
    </div>
  );
};

export default DietModal;