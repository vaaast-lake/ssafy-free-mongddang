import { api } from '@/shared/api/interceptors';

export const getNotification = () => {
  return api({
    method: 'GET',
    url: '/api/push/log',
  })
    .then((res) => {
      const data = res.data.data || null;
      return data;
    })
    .catch((err) => {
      console.log(err);
      return err;
    });
};

export const readNotification = async (notificationId: number) => {
  await api({
    method: 'PATCH',
    url: '/api/push/read',
    data: {
      NotificationId: notificationId,
    },
  })
    .then((res) => {
      console.log('알림 삭제 성공', res.data);
    })
    .catch((err) => {
      console.log(err);
      return err;
    });
};
