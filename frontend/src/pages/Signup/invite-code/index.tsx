/** @jsxImportSource @emotion/react */
import { Button } from '@/shared/ui/Button';
import { TextField } from '@/shared/ui/TextField';
import { Typography } from '@/shared/ui/Typography';
import { containerCss } from '../ui/invite-code.styles';
import { useMutation } from '@tanstack/react-query';
import { invitation } from '../api/api';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChangeEvent } from 'react';

export const InviteCode = () => {
  const nav = useNavigate();
  const [code, setCode] = useState('');

  const inviteMutation = useMutation({
    mutationFn: async () => {
      return await invitation(code);
    },
    onSuccess: async () => {
      alert('연결이 되었습니다.');
      nav('/');
    },
    onError: (error) => {
      console.error('회원가입 실패:', error);
      alert('연결이 실패하였습니다. 다시 시도해주세요');
    },
  });

  const handleCodeChange = (e: ChangeEvent<HTMLInputElement>) => {
    setCode(e.target.value);
  };

  const handleSubmit = () => {
    if (!code.trim()) {
      alert('초대 코드를 입력해주세요.');
      return;
    }
    inviteMutation.mutate();
  };

  return (
    <div css={containerCss}>
      <Typography color="dark" size="1.5" weight={700}>
        아이의 초대코드를
        <br />
        입력해주세요.
      </Typography>
      <TextField
        label=""
        defaultValue=""
        maxRows={10}
        type="text"
        variant="standard"
        value={code}
        onChange={handleCodeChange}
      />
      <Button
        style={{ width: '8rem' }}
        handler={handleSubmit}
        fullwidth
        color="primary"
        fontSize="1"
        variant="contained"
      >
        확인
      </Button>
    </div>
  );
};